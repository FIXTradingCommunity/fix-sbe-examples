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

import org.agrona.DirectBuffer;

/**
 * FIX Simple Open Framing Header encoder
 * 
 * @author Don Mendelson
 *
 */
public class SofhFrameDecoder {

  public static final short SBE_1_0_BIG_ENDIAN = (short) 0x5BE0;
  public static final short SBE_1_0_LITTLE_ENDIAN = (short) 0xEB50;

  private static final int ENCODING_OFFSET = 4;
  private static final int HEADER_LENGTH = 6;
  private static final int MESSAGE_LENGTH_OFFSET = 0;

  private DirectBuffer buffer;
  private int offset;


  public DirectBuffer buffer() {
    return buffer;
  }

  public int encodedLength() {
    return HEADER_LENGTH;
  }

  public short encoding() {
    return buffer.getShort(offset + ENCODING_OFFSET, java.nio.ByteOrder.BIG_ENDIAN);
  }

  public long messageLength() {
    return buffer.getInt(offset + MESSAGE_LENGTH_OFFSET, java.nio.ByteOrder.BIG_ENDIAN);

  }

  public int offset() {
    return offset;
  }

  public SofhFrameDecoder wrap(final DirectBuffer buffer, final int offset) {
    this.buffer = buffer;
    this.offset = offset;

    return this;
  }
}
