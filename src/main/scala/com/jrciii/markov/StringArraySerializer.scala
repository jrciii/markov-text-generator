package com.jrciii.markov

import org.mapdb.{DataInput2, DataOutput2, Serializer}

class StringArraySerializer extends Serializer[List[String]] {
  override def serialize(out: DataOutput2, value: List[String]): Unit = {
    out.packInt(value.size)
    value.foreach(out.writeUTF)
  }

  override def deserialize(input: DataInput2, available: Int): List[String] = {
    (0 until input.unpackInt()).map(_ => input.readUTF()).toList
  }
}
