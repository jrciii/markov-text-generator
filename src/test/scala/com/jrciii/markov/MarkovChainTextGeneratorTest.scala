package com.jrciii.markov

import org.scalatest.FreeSpec

class MarkovChainTextGeneratorTest extends FreeSpec {
  "The MarkovChainTextGenerator" - {
    "should generate text from a directory of bz2 markov chain files" in {
      println(MarkovChainTextGenerator.generateText("C:\\Users\\chympara\\gchain", 214379630L, 140))
    }
  }
}
