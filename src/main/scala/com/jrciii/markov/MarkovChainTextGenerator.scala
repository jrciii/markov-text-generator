package com.jrciii.markov

import java.io._

import net.openhft.chronicle.map.ChronicleMapBuilder
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.commons.io.FileUtils
import org.mapdb.{DBMaker, Serializer}

import scala.util.Random
import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
object MarkovChainTextGenerator {
  def makeMapDBMap = {
    DBMaker.tempFileDB.make.hashMap("gutenbergMarkovChain", new StringArraySerializer, new StringDoubleTupleSerializer).create().asScala
  }

  def makeChronicleMap(entries: Long) = {
    ChronicleMapBuilder
      .of(classOf[List[String]], classOf[List[(String,Double)]])
      .name("gutenberg-markov-chain")
      .averageKey(List("shaking","gait"))
      .averageValue(List(("hearin'",1.0), ("stoppin'",0.5)))
      .entries(entries)
      .createOrRecoverPersistedTo(new File("markovchain_chroniclemap"))
      .asScala
  }

  def readFileIntoChain(file: File, map: scala.collection.mutable.Map[List[String], List[(String, Double)]]) = {
    val bs = new BufferedInputStream(new FileInputStream(file))
    val input = new BufferedReader(new InputStreamReader(new CompressorStreamFactory().createCompressorInputStream(bs)))

    input.lines().iterator.asScala.foreach{l =>
      val tokens = l.split("\t").toList
      val probs = tokens.tail.map(_.split(" ") match {
        case Array(n,p) => (n,p.toDouble)
      })
      val key = tokens.head.split(" ").toList
      map.put(key,probs)
    }
  }

  def generateText(dir: String, words: Int): String = {
    val files = FileUtils.listFiles(new File(dir), Array("bz2"), false).asScala
    //val chronicleMap = makeChronicleMap(entries)
    val map = makeMapDBMap
    val futures = files.map(f => Future {readFileIntoChain(f, map)})
    Await.result(Future.sequence(futures), Duration.Inf)
    println("Done loading files into MapDB")
    generateStream(map, new Random()).take(words).mkString(" ")
  }

  def generateStream(chain: scala.collection.Map[List[String], List[(String, Double)]], randGen: Random) = {
    val first: (List[String], List[(String, Double)]) =
      chain.toStream.splitAt(randGen.nextInt(chain.size-1))._2.head
    def findNext(key: List[String]) = for {
      possible <- chain.get(key)
      pSort = possible.sortBy(_._2)
      prob = randGen.nextDouble()
      next <- pSort.find(_._2 > prob).map(_._1)
    } yield next

    def generate(key: List[String]): Stream[String] = {
      findNext(key) match {
        case Some(s) => s #:: generate(key.tail ++ List(s))
        case None => Stream()
      }
    }

    first._1.toStream ++ generate(first._1)
  }
}
