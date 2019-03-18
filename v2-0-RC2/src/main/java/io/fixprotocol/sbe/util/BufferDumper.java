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

package io.fixprotocol.sbe.util;

import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Prints the contents of a buffer in hex and text
 * 
 * @author Don Mendelson
 * 
 */
public final class BufferDumper {

  /**
   * Print a byte array as hex and text
   * 
   * @param bytes data to print
   * @param index index from start of data buffer to print
   * @param width width of output as number of bytes of file to print per line
   * @param messageSize maximum number of bytes to print
   * @param out output stream
   * @throws UnsupportedEncodingException if character set conversion fails
   * @throws IndexOutOfBoundsException if messageSize > bytes.length
   */
  public static void print(byte[] bytes, int index, int width, long messageSize, PrintStream out, Charset charset)
      throws UnsupportedEncodingException {
    if (messageSize > bytes.length) {
      throw new IndexOutOfBoundsException();
    }

    for (int i = 0; i < messageSize; i += width) {
      int lineWidth = Math.min((int) messageSize - i, width);
      String hexLine = printHexLine(bytes, index + i, lineWidth);
      out.print(hexLine);
      for (int j = hexLine.length(); j < width * 3; j++) {
        out.print(' ');
      }
      out.println(printTextLine(bytes, index + i, lineWidth, charset));
    }
  }

  /**
   * Print a byte array as hex and text
   * 
   * @param bytes data to print
   * @param width width of output as number of bytes of file to print per line
   * @param messageSize maximum number of bytes to print
   * @param out output stream
   * @throws UnsupportedEncodingException if character set conversion fails
   */
  public static void print(byte[] bytes, int width, long messageSize, PrintStream out, Charset charset)
      throws UnsupportedEncodingException {
    print(bytes, 0, width, messageSize, out, charset);
  }

  /**
   * Print a byte array as hex and text
   * 
   * @param buffer buffer to print
   * @param width width of output as number of bytes of file to print per line
   * @param out output stream
   * @throws UnsupportedEncodingException if character set conversion fails
   */
  public static void print(ByteBuffer buffer, int width, PrintStream out, Charset charset)
      throws UnsupportedEncodingException {
    ByteBuffer buffer2 = buffer.duplicate();
    buffer2.order(buffer.order());
    if (buffer2.position() == buffer2.limit()) {
      buffer2.rewind();
    }
    byte[] bytes = new byte[buffer2.remaining()];
    buffer2.get(bytes);
    print(bytes, 0, width, bytes.length, out, charset);
  }

  private static String printTextLine(byte[] bytes, int index, int width, Charset charset)
      throws UnsupportedEncodingException {
    StringWriter writer = new StringWriter();
    if (index < bytes.length) {
      writer.append(":");
      writer.append(new String(bytes, index, Math.min(width, bytes.length - index), charset)
          .replaceAll("[^\\x20-\\x7E]", " "));
    }
    return writer.toString();
  }

  private static String printHexLine(byte[] bytes, int offset, int width) {
    StringWriter writer = new StringWriter();
    for (int index = 0; index < width; index++) {
      if (index + offset < bytes.length) {
        writer.append(String.format("%02x ", bytes[index + offset]));
      }
    }
    return writer.toString();
  }

}
