package com.jrciii.markov

import java.io.File

import org.scalatest.FreeSpec

class MarkovChainTextGeneratorTest extends FreeSpec {
  "The MarkovChainTextGenerator" - {
    "should generate text from a directory of markov chain files" in {
      MarkovChainTextGenerator
        .generateFile(
          "C:\\Users\\no_u\\gchain",
          121393,
          "_2",
          "Literature is",
          new File("output.txt"))
    }
  }
}
