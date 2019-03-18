/*
 * Copyright 2015-2019 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package io.fixprotocol.sbe.examples;

import org.agrona.MutableDirectBuffer;

/**
 * FIX Simple Open Framing Header encoder
 * 
 * @author Don Mendelson
 *
 */
public class SofhFrameEncoder {

  public static final short SBE_1_0_BIG_ENDIAN = (short) 0x5BE0;
  public static final short SBE_1_0_LITTLE_ENDIAN = (short) 0xEB50;
  
  private static final int ENCODING_OFFSET = 4;
  private static final int HEADER_LENGTH = 6;
  private static final int MESSAGE_LENGTH_OFFSET = 0;

  private MutableDirectBuffer buffer;
  private int offset;


  public MutableDirectBuffer buffer()
  {
      return buffer;
  }

  public int encodedLength() {
    return HEADER_LENGTH;
  }

  public SofhFrameEncoder encoding(short encoding) {
    buffer.putShort(offset + ENCODING_OFFSET, encoding, java.nio.ByteOrder.BIG_ENDIAN);
    return this;
  }

  public SofhFrameEncoder messageLength(long messageLength) {
    buffer.putInt(offset + MESSAGE_LENGTH_OFFSET, (int) (messageLength & 0xffffffff),
        java.nio.ByteOrder.BIG_ENDIAN);
    return this;
  }

  public int offset()
  {
      return offset;
  }

  public SofhFrameEncoder wrap(final MutableDirectBuffer buffer, final int offset) {
    this.buffer = buffer;
    this.offset = offset;

    return this;
  }
}
