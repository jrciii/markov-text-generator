package com.jrciii.markov

import java.io._
import scala.util.control.Breaks._
import org.apache.commons.io.FileUtils
import org.iq80.leveldb.{Options, _}
import org.iq80.leveldb.impl.Iq80DBFactory._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Random, Try}
object MarkovChainTextGenerator {
  def makeLevelDB(suffix: String) = {
    val options = new Options()
    options.cacheSize(4 * 1024*1024*1024)
    options.createIfMissing(true)
    factory.open(new File("leveldb" + suffix), options)
  }

  def readFileIntoChain(file: File, db: DB) = {
    val input = new BufferedReader(new InputStreamReader(new FileInputStream(file)))
    val batch = db.createWriteBatch()
    var next = input.read
    var nextChar = next.toChar
    breakable {
      while (next != -1) {
        val keyBuilder = new StringBuilder(nextChar.toString)
        next = input.read
        nextChar = next.toChar
        while (next != -1
          && nextChar != '\t') {
          keyBuilder.append(nextChar)
          next = input.read
          nextChar = next.toChar
        }
        if (keyBuilder.isEmpty) {
          break
        }
        val valBuilder = new StringBuilder
        next = input.read
        nextChar = next.toChar
        while (next != -1
          && nextChar != '\n') {
          valBuilder.append(nextChar)
          next = input.read
          nextChar = next.toChar
        }
        if (valBuilder.isEmpty) {
          break
        }
        next = input.read
        nextChar = next.toChar
        batch.put(bytes(keyBuilder.toString), bytes(valBuilder.toString))
      }
    }
    db.write(batch)
    batch.close()
    input.close()
  }

  val spaceBytes = bytes(" ")
  def generateFile(dir: String, words: Int, dbSuffix: String, seed: String, outFile: File) {
    val toLoad = !new File("leveldb" + dbSuffix).exists()
    val map = makeLevelDB(dbSuffix)
    if (toLoad) {
      val files = FileUtils.listFiles(new File(dir), null, false).asScala
      val futures = files.map(f => Future {
        readFileIntoChain(f, map)
      })
      Await.result(Future.sequence(futures), Duration.Inf)
      println("Done loading files into LevelDB")
    }
    def probParse(prob: String) = prob.split("\t").map(e => {
      val s = e.split(" ")
      (s(0), s(1).toDouble)
    }).toList

    val stream = generateStream(seed,
      k => Try(map.get(bytes(k)))
        .fold({
          case _: Exception => None
          case t => throw(t)
        }, b => Some(probParse(new String(b)))),
      new Random()).take(words)

    val writer = new BufferedOutputStream(new FileOutputStream(outFile))
    val last = stream.tail.foldLeft(stream.head)((last,word) => {
      writer.write(bytes(last))
      writer.write(spaceBytes)
      word
    })
    writer.write(bytes(last))
    writer.close()
  }

  def generateStream(first: String,
                     chain: String => Option[List[(String, Double)]],
                     randGen: Random) = {
    def findNext(key: String) = for {
      possible <- chain(key)
      pSort = possible.sortBy(_._2)
      prob = randGen.nextDouble()
      next <- pSort.find(_._2 > prob).map(_._1)
    } yield next

    def generate(key: String): Stream[String] = {
      findNext(key) match {
        case Some(s) => s #:: generate(key.split(" ",2).last + " " + s)
        case None => Stream()
      }
    }

    Stream(first) ++ generate(first)
  }
}
