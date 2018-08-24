package com.jrciii.markov

import org.mapdb.{DataInput2, DataOutput2, Serializer}

class StringDoubleTupleSerializer extends Serializer[List[(String,Double)]] {
  override def serialize(out: DataOutput2, value: List[(String, Double)]) {
    out.packInt(value.size)
    value.foreach(t => {
      out.writeUTF(t._1)
      out.writeDouble(t._2)
    })
  }

  override def deserialize(input: DataInput2, available: Int): List[(String, Double)] = {
    val size = input.unpackInt
    (0 until size).map(_ => (input.readUTF(),input.readDouble())).toList
  }
}
