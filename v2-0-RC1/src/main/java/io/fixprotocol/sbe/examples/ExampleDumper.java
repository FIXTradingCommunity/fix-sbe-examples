/**
 * Copyright 2017 FIX Protocol Ltd
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import io.fixprotocol.sbe.examples.messages.BusinessMessageRejectDecoder;
import io.fixprotocol.sbe.examples.messages.BusinessMessageRejectEncoder;
import io.fixprotocol.sbe.examples.messages.BusinessRejectReasonEnum;
import io.fixprotocol.sbe.examples.messages.DecimalEncodingDecoder;
import io.fixprotocol.sbe.examples.messages.DecimalEncodingEncoder;
import io.fixprotocol.sbe.examples.messages.ExecTypeEnum;
import io.fixprotocol.sbe.examples.messages.ExecutionReportDecoder;
import io.fixprotocol.sbe.examples.messages.ExecutionReportDecoder.FillsGrpDecoder;
import io.fixprotocol.sbe.examples.messages.ExecutionReportEncoder;
import io.fixprotocol.sbe.examples.messages.ExecutionReportEncoder.FillsGrpEncoder;
import io.fixprotocol.sbe.examples.messages.GroupSizeEncodingDecoder;
import io.fixprotocol.sbe.examples.messages.MONTH_YEARDecoder;
import io.fixprotocol.sbe.examples.messages.MONTH_YEAREncoder;
import io.fixprotocol.sbe.examples.messages.MessageHeaderDecoder;
import io.fixprotocol.sbe.examples.messages.MessageHeaderEncoder;
import io.fixprotocol.sbe.examples.messages.NewOrderSingleDecoder;
import io.fixprotocol.sbe.examples.messages.NewOrderSingleEncoder;
import io.fixprotocol.sbe.examples.messages.OrdStatusEnum;
import io.fixprotocol.sbe.examples.messages.OrdTypeEnum;
import io.fixprotocol.sbe.examples.messages.QtyEncodingDecoder;
import io.fixprotocol.sbe.examples.messages.QtyEncodingEncoder;
import io.fixprotocol.sbe.examples.messages.SideEnum;
import io.fixprotocol.sbe.util.BufferDumper;


/**
 * Generates SBE examples
 * 
 * Messages are encoded with Simple Open Framing Header. Offsets are from beginning of block.
 * Default style is markdown.
 * 
 * @author Don Mendelson
 *
 */
public class ExampleDumper {

  /**
   * Markdown begin block
   */
  public static final String MARKDOWN_BLOCK_BEGIN = "```\n";

  /**
   * Markdown end block
   */
  public static final String MARKDOWN_BLOCK_END = "```\n";

  /**
   * Markdown begin heading
   */
  public static final String MARKDOWN_HEADING_BEGIN = "### ";

  /**
   * Markdown end heading
   */
  public static final String MARKDOWN_HEADING_END = "";

  /**
   * Markdown begin literal
   */
  public static final String MARKDOWN_LITERAL_BEGIN = "`";

  /**
   * Markdown end literal
   */
  public static final String MARKDOWN_LITERAL_END = "`";

  /**
   * Markdown table column delimiter
   */
  public static final String MARKDOWN_TABLE_COLUMN_DELIM = "|";

  /**
   * Markdown begin table row
   */
  public static final String MARKDOWN_TABLE_ROW_BEGIN = "|";

  /**
   * Markdown end table row
   */
  public static final String MARKDOWN_TABLE_ROW_END = "|";

  private final MessageHeaderDecoder mhDecoder = new MessageHeaderDecoder();
  private final GroupSizeEncodingDecoder ghDecoder = new GroupSizeEncodingDecoder();

  /**
   * Output all examples
   * 
   * @param args first argument is name of an output file (optional). If not supplied, output goes
   *        to {@code System.out}.
   * @throws UnsupportedEncodingException if encoding conversion fails
   * @throws FileNotFoundException if the specified file cannot be created
   */
  public static void main(String[] args)
      throws UnsupportedEncodingException, FileNotFoundException {
    PrintStream stream = System.out;
    if (args.length > 0) {
      stream = new PrintStream(new FileOutputStream(args[0]));
    }
    ExampleDumper dumper = new ExampleDumper();
    dumper.dumpAll(stream);
  }

  private static String wireFormat(byte[] bytes, int offset, int width) {
    StringWriter writer = new StringWriter();
    for (int index = 0; index < width; index++) {
      if (index + offset < bytes.length) {
        writer.append(String.format("%02x", bytes[index + offset]));
      }
    }
    return writer.toString();
  }

  private String blockBegin = MARKDOWN_BLOCK_BEGIN;
  private String blockEnd = MARKDOWN_BLOCK_END;
  private String headingBegin = MARKDOWN_HEADING_BEGIN;
  private String headingEnd = MARKDOWN_HEADING_END;
  private String literalBegin = MARKDOWN_LITERAL_BEGIN;
  private String literalEnd = MARKDOWN_LITERAL_END;
  private String tableColumnDelim = MARKDOWN_TABLE_COLUMN_DELIM;
  private String tableRowBegin = MARKDOWN_TABLE_ROW_BEGIN;
  private String tableRowEnd = MARKDOWN_TABLE_ROW_END;

  public void dump(byte[] bytes, int offset, PrintStream out) throws UnsupportedEncodingException {
    out.print(blockBegin);
    BufferDumper.print(bytes, 16, offset, out);
    out.print(blockEnd);
  }

  public void dumpAll(PrintStream out) throws UnsupportedEncodingException {
    byte bytes[] = new byte[128];
    int size = encodeOrderMsg(bytes);
    heading("Wire format", out);
    dump(bytes, size, out);
    heading("Interpretation", out);
    interpretOrderMsg(bytes, size, out);
    Arrays.fill(bytes, (byte) 0);
    size = encodeExecutionReport(bytes);
    heading("Wire format", out);
    dump(bytes, size, out);
    heading("Interpretation", out);
    interpretExecutionReport(bytes, size, out);
    Arrays.fill(bytes, (byte) 0);
    size = encodeBusinessMessageReject(bytes);
    heading("Wire format", out);
    dump(bytes, size, out);
    heading("Interpretation", out);
    interpretBusinessMessageReject(bytes, size, out);
  }

  public int encodeBusinessMessageReject(byte bytes[]) throws UnsupportedEncodingException {
    SofhFrameEncoder sofhEncoder = new SofhFrameEncoder();
    MessageHeaderEncoder mhEncoder = new MessageHeaderEncoder();
    BusinessMessageRejectEncoder bmrEncoder = new BusinessMessageRejectEncoder();
    MutableDirectBuffer buffer = new UnsafeBuffer(bytes);

    int offset = 0;
    sofhEncoder.wrap(buffer, offset);
    sofhEncoder.encoding(SofhFrameEncoder.SBE_1_0_LITTLE_ENDIAN);
    offset += sofhEncoder.encodedLength();

    mhEncoder.wrap(buffer, offset);
    mhEncoder.blockLength(bmrEncoder.sbeBlockLength()).templateId(bmrEncoder.sbeTemplateId())
        .schemaId(bmrEncoder.sbeSchemaId()).version(bmrEncoder.sbeSchemaVersion()).numGroups(0)
        .numVarDataFields(1);
    offset += mhEncoder.encodedLength();

    bmrEncoder.wrap(buffer, offset);
    bmrEncoder.putBusinesRejectRefId("ORD00001".getBytes(), 0);
    bmrEncoder.businessRejectReason(BusinessRejectReasonEnum.NotAuthorized);
    byte[] text = "Not authorized to trade that instrument".getBytes();
    bmrEncoder.putText(text, 0, text.length);
    offset += bmrEncoder.encodedLength();

    sofhEncoder.messageLength(offset);
    return offset;
  }

  public int encodeExecutionReport(byte[] bytes) throws UnsupportedEncodingException {
    SofhFrameEncoder sofhEncoder = new SofhFrameEncoder();
    MessageHeaderEncoder mhEncoder = new MessageHeaderEncoder();
    ExecutionReportEncoder erEncoder = new ExecutionReportEncoder();

    MutableDirectBuffer buffer = new UnsafeBuffer(bytes);

    int offset = 0;
    sofhEncoder.wrap(buffer, offset);
    sofhEncoder.encoding(SofhFrameEncoder.SBE_1_0_LITTLE_ENDIAN);
    offset += sofhEncoder.encodedLength();

    mhEncoder.wrap(buffer, offset);
    mhEncoder.blockLength(erEncoder.sbeBlockLength()).templateId(erEncoder.sbeTemplateId())
        .schemaId(erEncoder.sbeSchemaId()).version(erEncoder.sbeSchemaVersion()).numGroups(1)
        .numVarDataFields(0);
    offset += mhEncoder.encodedLength();

    erEncoder.wrap(buffer, offset);
    erEncoder.putOrderID("O0000001".getBytes(), 0);
    erEncoder.putExecID("EXEC0000".getBytes(), 0);
    erEncoder.execType(ExecTypeEnum.Trade);
    erEncoder.ordStatus(OrdStatusEnum.PartialFilled);
    erEncoder.putSymbol("GEM4\u0000\u0000\u0000\u0000".getBytes(), 0);
    MONTH_YEAREncoder matEncoder = erEncoder.maturityMonthYear();
    matEncoder.year(2014);
    matEncoder.month((short) 6);
    matEncoder.day(MONTH_YEAREncoder.dayNullValue());
    matEncoder.week(MONTH_YEAREncoder.weekNullValue());
    erEncoder.side(SideEnum.Buy);
    QtyEncodingEncoder qtyEncoder = erEncoder.leavesQty();
    qtyEncoder.mantissa(1);
    qtyEncoder = erEncoder.cumQty();
    qtyEncoder.mantissa(6);
    LocalDate localDate = LocalDate.of(2013, 10, 11);
    erEncoder.tradeDate((int) localDate.toEpochDay());
    FillsGrpEncoder fillGrpEncoder = erEncoder.fillsGrpCount(2);
    fillGrpEncoder = fillGrpEncoder.next();
    DecimalEncodingEncoder decEncoder = fillGrpEncoder.fillPx();
    decEncoder.mantissa(99610);
    qtyEncoder = fillGrpEncoder.fillQty();
    qtyEncoder.mantissa(2);
    fillGrpEncoder = fillGrpEncoder.next();
    decEncoder = fillGrpEncoder.fillPx();
    decEncoder.mantissa(99620);
    qtyEncoder = fillGrpEncoder.fillQty();
    qtyEncoder.mantissa(4);
    offset += erEncoder.encodedLength();

    sofhEncoder.messageLength(offset);
    return offset;
  }

  public int encodeOrderMsg(byte bytes[]) throws UnsupportedEncodingException {
    SofhFrameEncoder sofhEncoder = new SofhFrameEncoder();
    MessageHeaderEncoder mhEncoder = new MessageHeaderEncoder();
    NewOrderSingleEncoder nosEncoder = new NewOrderSingleEncoder();

    MutableDirectBuffer buffer = new UnsafeBuffer(bytes);

    int offset = 0;
    sofhEncoder.wrap(buffer, offset);
    sofhEncoder.encoding(SofhFrameEncoder.SBE_1_0_LITTLE_ENDIAN);
    offset += sofhEncoder.encodedLength();

    mhEncoder.wrap(buffer, offset);
    mhEncoder.blockLength(nosEncoder.sbeBlockLength()).templateId(nosEncoder.sbeTemplateId())
        .schemaId(nosEncoder.sbeSchemaId()).version(nosEncoder.sbeSchemaVersion()).numGroups(0)
        .numVarDataFields(0);
    offset += mhEncoder.encodedLength();

    nosEncoder.wrap(buffer, offset);
    nosEncoder.putClOrdId("ORD00001".getBytes(), 0);
    nosEncoder.putAccount("ACCT01\u0000\u0000".getBytes(), 0);
    nosEncoder.putSymbol("GEM4\u0000\u0000\u0000\u0000".getBytes(), 0);
    nosEncoder.side(SideEnum.Buy);
    nosEncoder.transactTime().time(TimeUnit.MILLISECONDS.toNanos(Instant.now().toEpochMilli()));
    QtyEncodingEncoder qtyEncoder = nosEncoder.orderQty();
    qtyEncoder.mantissa(7);
    nosEncoder.ordType(OrdTypeEnum.Limit);
    DecimalEncodingEncoder decEncoder = nosEncoder.price();
    decEncoder.mantissa(99610);
    decEncoder = nosEncoder.stopPx();
    decEncoder.mantissa(DecimalEncodingEncoder.mantissaNullValue());
    offset += nosEncoder.encodedLength();

    sofhEncoder.messageLength(offset);
    return offset;
  }

  public String getBlockBegin() {
    return blockBegin;
  }

  public String getBlockEnd() {
    return blockEnd;
  }

  public String getHeadingEnd() {
    return headingEnd;
  }

  public String getLiteralBegin() {
    return literalBegin;
  }

  public String getLiteralEnd() {
    return literalEnd;
  }

  public String getTableColumnDelim() {
    return tableColumnDelim;
  }

  public String getTableRowBegin() {
    return tableRowBegin;
  }

  public String getTableRowEnd() {
    return tableRowEnd;
  }

  public void interpretBusinessMessageReject(byte[] bytes, int size, PrintStream out) {
    DirectBuffer buffer = new UnsafeBuffer(bytes);
    interpretTableHeader(out);

    int offset = 0;
    offset = interpretFramingHeader(bytes, out, buffer, offset);
    mhDecoder.wrap(buffer, offset);
    offset = interpretMessageHeader(bytes, out);

    BusinessMessageRejectDecoder bmrDecoder = new BusinessMessageRejectDecoder();
    bmrDecoder.wrap(buffer, offset, mhDecoder.blockLength(), mhDecoder.version());
    String businesRejectRefId = bmrDecoder.businesRejectRefId();
    interpretRow(
        wireFormat(bytes, offset + BusinessMessageRejectDecoder.businesRejectRefIdEncodingOffset(),
            BusinessMessageRejectDecoder.businesRejectRefIdEncodingLength()),
        BusinessMessageRejectDecoder.businesRejectRefIdId(), "BusinesRejectRefId",
        BusinessMessageRejectDecoder.businesRejectRefIdEncodingOffset(),
        BusinessMessageRejectDecoder.businesRejectRefIdEncodingLength(), businesRejectRefId, out);
    BusinessRejectReasonEnum rejectReason = bmrDecoder.businessRejectReason();
    interpretRow(
        wireFormat(bytes,
            offset + BusinessMessageRejectDecoder.businessRejectReasonEncodingOffset(),
            BusinessMessageRejectDecoder.businessRejectReasonEncodingLength()),
        BusinessMessageRejectDecoder.businessRejectReasonId(), "BusinessRejectReason",
        BusinessMessageRejectDecoder.businessRejectReasonEncodingOffset(),
        BusinessMessageRejectDecoder.businessRejectReasonEncodingLength(), rejectReason.name(),
        out);
    offset += bmrDecoder.sbeBlockLength();
    byte[] text = new byte[128];
    int textLength = bmrDecoder.getText(text, 0, text.length);
    interpretRow(wireFormat(bytes, offset + 2, 6) + "...", BusinessMessageRejectDecoder.textId(),
        "Text", 0, textLength, new String(text, 0, textLength), out);
  }

  public void interpretExecutionReport(byte[] bytes, int size, PrintStream out) {
    DirectBuffer buffer = new UnsafeBuffer(bytes);
    interpretTableHeader(out);

    int offset = 0;
    offset = interpretFramingHeader(bytes, out, buffer, offset);
    mhDecoder.wrap(buffer, offset);
    offset = interpretMessageHeader(bytes, out);
    ExecutionReportDecoder erDecoder = new ExecutionReportDecoder();
    erDecoder.wrap(buffer, offset, mhDecoder.blockLength(), mhDecoder.version());
    String orderId = erDecoder.orderID();
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.orderIDEncodingOffset(),
            ExecutionReportDecoder.orderIDEncodingLength()),
        ExecutionReportDecoder.orderIDId(), "OrderID",
        ExecutionReportDecoder.orderIDEncodingLength(),
        ExecutionReportDecoder.orderIDEncodingLength(), orderId, out);
    String execId = erDecoder.execID();
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.execIDEncodingOffset(),
            ExecutionReportDecoder.execIDEncodingLength()),
        ExecutionReportDecoder.execIDId(), "ExecID", ExecutionReportDecoder.execIDEncodingLength(),
        ExecutionReportDecoder.execIDEncodingLength(), execId, out);
    OrdStatusEnum ordStatus = erDecoder.ordStatus();
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.ordStatusEncodingOffset(),
            ExecutionReportDecoder.ordStatusEncodingLength()),
        ExecutionReportDecoder.ordStatusId(), "OrdStatus",
        ExecutionReportDecoder.ordStatusEncodingLength(),
        ExecutionReportDecoder.ordStatusEncodingLength(), ordStatus.name(), out);
    String symbol = erDecoder.symbol();
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.symbolEncodingOffset(),
            ExecutionReportDecoder.symbolEncodingLength()),
        ExecutionReportDecoder.symbolId(), "Symbol", ExecutionReportDecoder.symbolEncodingOffset(),
        ExecutionReportDecoder.symbolEncodingLength(), symbol, out);
    MONTH_YEARDecoder monthYearDecoder = erDecoder.maturityMonthYear();
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.maturityMonthYearEncodingOffset(),
            ExecutionReportDecoder.maturityMonthYearEncodingLength()),
        ExecutionReportDecoder.maturityMonthYearId(), "MaturityMonthYear",
        ExecutionReportDecoder.maturityMonthYearEncodingOffset(),
        ExecutionReportDecoder.maturityMonthYearEncodingLength(),
        monthYearToString(monthYearDecoder), out);
    SideEnum side = erDecoder.side();
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.sideEncodingOffset(),
            ExecutionReportDecoder.sideEncodingLength()),
        ExecutionReportDecoder.sideId(), "Side", ExecutionReportDecoder.sideEncodingLength(),
        ExecutionReportDecoder.sideEncodingLength(), side.name(), out);
    QtyEncodingDecoder qtyDecoder = erDecoder.leavesQty();
    int mantissa = qtyDecoder.mantissa();
    byte exponent = qtyDecoder.exponent();
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.leavesQtyEncodingOffset(),
            ExecutionReportDecoder.leavesQtyEncodingLength()),
        ExecutionReportDecoder.leavesQtyId(), "LeavesQty",
        ExecutionReportDecoder.leavesQtyEncodingOffset(),
        ExecutionReportDecoder.leavesQtyEncodingLength(), decimalToString(mantissa, exponent), out);
    qtyDecoder = erDecoder.cumQty();
    int cumMantissa = qtyDecoder.mantissa();
    byte cumExponent = qtyDecoder.exponent();
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.cumQtyEncodingOffset(),
            ExecutionReportDecoder.cumQtyEncodingLength()),
        ExecutionReportDecoder.cumQtyId(), "CumQty", ExecutionReportDecoder.cumQtyEncodingOffset(),
        ExecutionReportDecoder.cumQtyEncodingLength(), decimalToString(cumMantissa, cumExponent),
        out);
    int epochDay = erDecoder.tradeDate();
    LocalDate tradeDate = LocalDate.ofEpochDay(epochDay);
    interpretRow(
        wireFormat(bytes, offset + ExecutionReportDecoder.tradeDateEncodingOffset(),
            ExecutionReportDecoder.tradeDateEncodingLength()),
        ExecutionReportDecoder.tradeDateId(), "TradeDate",
        ExecutionReportDecoder.tradeDateEncodingOffset(),
        ExecutionReportDecoder.tradeDateEncodingLength(), tradeDate.toString(), out);
    offset += mhDecoder.blockLength();
    FillsGrpDecoder fillsGrp = erDecoder.fillsGrp();
    ghDecoder.wrap(buffer, offset);
    interpretGroupHeader(bytes, out);
    offset += FillsGrpDecoder.sbeHeaderSize();
    while (fillsGrp.hasNext()) {
      fillsGrp = fillsGrp.next();
      DecimalEncodingDecoder decimalDecoder = fillsGrp.fillPx();
      long priceMantissa = decimalDecoder.mantissa();
      byte priceExponent = decimalDecoder.exponent();
      interpretRow(
          wireFormat(bytes, offset + FillsGrpDecoder.fillPxEncodingOffset(),
              FillsGrpDecoder.fillPxEncodingLength()),
          FillsGrpDecoder.fillPxId(), "FillPx", FillsGrpDecoder.fillPxEncodingOffset(),
          FillsGrpDecoder.fillPxEncodingLength(), decimalToString(priceMantissa, priceExponent),
          out);
      QtyEncodingDecoder fillQtyDecoder = fillsGrp.fillQty();
      int fillMantissa = fillQtyDecoder.mantissa();
      byte fillExponent = fillQtyDecoder.exponent();
      interpretRow(
          wireFormat(bytes, offset + FillsGrpDecoder.fillQtyEncodingOffset(),
              FillsGrpDecoder.fillQtyEncodingLength()),
          FillsGrpDecoder.fillQtyId(), "FillQty", FillsGrpDecoder.fillQtyEncodingOffset(),
          FillsGrpDecoder.fillQtyEncodingLength(), decimalToString(fillMantissa, fillExponent),
          out);
      offset += FillsGrpDecoder.sbeBlockLength();
    }

  }

  public void interpretOrderMsg(byte[] bytes, int size, PrintStream out) {
    DirectBuffer buffer = new UnsafeBuffer(bytes);
    interpretTableHeader(out);

    int offset = 0;
    offset = interpretFramingHeader(bytes, out, buffer, offset);
    mhDecoder.wrap(buffer, offset);
    offset = interpretMessageHeader(bytes, out);
    NewOrderSingleDecoder nosDecoder = new NewOrderSingleDecoder();
    nosDecoder.wrap(buffer, offset, mhDecoder.blockLength(), mhDecoder.version());
    String clOrdId = nosDecoder.clOrdId();
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.clOrdIdEncodingOffset(),
            NewOrderSingleDecoder.clOrdIdEncodingLength()),
        NewOrderSingleDecoder.clOrdIdId(), "ClOrdId", NewOrderSingleDecoder.clOrdIdEncodingOffset(),
        NewOrderSingleDecoder.clOrdIdEncodingLength(), clOrdId, out);
    String account = nosDecoder.account();
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.accountEncodingOffset(),
            NewOrderSingleDecoder.accountEncodingLength()),
        NewOrderSingleDecoder.accountId(), "Account", NewOrderSingleDecoder.accountEncodingOffset(),
        NewOrderSingleDecoder.accountEncodingLength(), account, out);
    String symbol = nosDecoder.symbol();
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.symbolEncodingOffset(),
            NewOrderSingleDecoder.symbolEncodingLength()),
        NewOrderSingleDecoder.symbolId(), "Symbol", NewOrderSingleDecoder.symbolEncodingOffset(),
        NewOrderSingleDecoder.symbolEncodingLength(), symbol, out);
    SideEnum side = nosDecoder.side();
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.sideEncodingOffset(),
            NewOrderSingleDecoder.sideEncodingLength()),
        NewOrderSingleDecoder.sideId(), "Side", NewOrderSingleDecoder.sideEncodingOffset(),
        NewOrderSingleDecoder.sideEncodingLength(), side.name(), out);
    long transactTime = nosDecoder.transactTime().time();
    long epochMilli = TimeUnit.NANOSECONDS.toMillis(transactTime);
    Instant instant = Instant.ofEpochMilli(epochMilli);
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.transactTimeEncodingOffset(),
            NewOrderSingleDecoder.transactTimeEncodingLength()),
        NewOrderSingleDecoder.transactTimeId(), "TransactTime",
        NewOrderSingleDecoder.transactTimeEncodingOffset(),
        NewOrderSingleDecoder.transactTimeEncodingLength(), instant.toString(), out);
    QtyEncodingDecoder qtyDecoder = nosDecoder.orderQty();
    int mantissa = qtyDecoder.mantissa();
    byte exponent = qtyDecoder.exponent();
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.orderQtyEncodingOffset(),
            NewOrderSingleDecoder.orderQtyEncodingLength()),
        NewOrderSingleDecoder.orderQtyId(), "OrderQty",
        NewOrderSingleDecoder.orderQtyEncodingOffset(),
        NewOrderSingleDecoder.orderQtyEncodingLength(), decimalToString(mantissa, exponent), out);
    OrdTypeEnum ordType = nosDecoder.ordType();
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.ordTypeEncodingOffset(),
            NewOrderSingleDecoder.ordTypeEncodingLength()),
        NewOrderSingleDecoder.ordTypeId(), "OrdType", NewOrderSingleDecoder.ordTypeEncodingOffset(),
        NewOrderSingleDecoder.ordTypeEncodingLength(), ordType.name(), out);
    DecimalEncodingDecoder decimalDecoder = nosDecoder.price();
    long priceMantissa = decimalDecoder.mantissa();
    byte priceExponent = decimalDecoder.exponent();
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.priceEncodingOffset(),
            NewOrderSingleDecoder.priceEncodingLength()),
        NewOrderSingleDecoder.priceId(), "Price", NewOrderSingleDecoder.priceEncodingOffset(),
        NewOrderSingleDecoder.priceEncodingLength(), decimalToString(priceMantissa, priceExponent),
        out);
    decimalDecoder = nosDecoder.stopPx();
    priceMantissa = decimalDecoder.mantissa();
    priceExponent = decimalDecoder.exponent();
    interpretRow(
        wireFormat(bytes, offset + NewOrderSingleDecoder.stopPxEncodingOffset(),
            NewOrderSingleDecoder.stopPxEncodingLength()),
        NewOrderSingleDecoder.stopPxId(), "StopPx", NewOrderSingleDecoder.stopPxEncodingOffset(),
        NewOrderSingleDecoder.stopPxEncodingLength(), decimalToString(priceMantissa, priceExponent),
        out);
  }

  public void setBlockBegin(String blockBegin) {
    this.blockBegin = blockBegin;
  }

  public void setBlockEnd(String blockEnd) {
    this.blockEnd = blockEnd;
  }

  public void setHeadingEnd(String headingEnd) {
    this.headingEnd = headingEnd;
  }

  public void setLiteralBegin(String literalBegin) {
    this.literalBegin = literalBegin;
  }

  public void setLiteralEnd(String literalEnd) {
    this.literalEnd = literalEnd;
  }

  public void setTableColumnDelim(String tableColumnDelim) {
    this.tableColumnDelim = tableColumnDelim;
  }

  public void setTableRowBegin(String tableRowBegin) {
    this.tableRowBegin = tableRowBegin;
  }

  public void setTableRowEnd(String tableRowEnd) {
    this.tableRowEnd = tableRowEnd;
  }

  private String decimalToString(int mantissa, int exponent) {
    if (mantissa == -2147483648) {
      return "null";
    } else {
      return BigDecimal.valueOf(mantissa, -exponent).toString();
    }
  }

  private String decimalToString(long mantissa, int exponent) {
    if (mantissa == -9223372036854775808L) {
      return "null";
    } else {
      return BigDecimal.valueOf(mantissa, -exponent).toString();
    }
  }

  private int interpretFramingHeader(byte[] bytes, PrintStream out, DirectBuffer buffer,
      int offset) {
    SofhFrameDecoder sofhDecoder = new SofhFrameDecoder();
    sofhDecoder.wrap(buffer, offset);
    long messageLength = sofhDecoder.messageLength();
    interpretRow(wireFormat(bytes, offset, 4), 0, "SOFH message length", 0, 4,
        String.format("%d", messageLength), out);
    short encoding = sofhDecoder.encoding();
    String encodingName = null;
    switch (encoding) {
      case SofhFrameEncoder.SBE_1_0_LITTLE_ENDIAN:
        encodingName = "SBE little-endian";
        break;
      case SofhFrameEncoder.SBE_1_0_BIG_ENDIAN:
        encodingName = "SBE big-endian";
        break;
      default:
        encodingName = "Unknown encoding";
    }
    interpretRow(wireFormat(bytes, offset + 4, 2), 0, "SOFH encoding", 4, 2, encodingName, out);

    return offset + sofhDecoder.encodedLength();
  }

  private int interpretGroupHeader(byte[] bytes, PrintStream out) {
    int blockLength = ghDecoder.blockLength();
    interpretRow(
        wireFormat(bytes, ghDecoder.offset() + GroupSizeEncodingDecoder.blockLengthEncodingOffset(),
            GroupSizeEncodingDecoder.blockLengthEncodingLength()),
        0, "Group block length", GroupSizeEncodingDecoder.blockLengthEncodingOffset(),
        GroupSizeEncodingDecoder.blockLengthEncodingLength(), String.format("%d", blockLength), out);

    int numInGroup = ghDecoder.numInGroup();
    interpretRow(
        wireFormat(bytes, ghDecoder.offset() + GroupSizeEncodingDecoder.numGroupsEncodingOffset(),
            GroupSizeEncodingDecoder.numGroupsEncodingLength()),
        0, "NumInGroup", GroupSizeEncodingDecoder.numGroupsEncodingOffset(),
        GroupSizeEncodingDecoder.numGroupsEncodingLength(), String.format("%d", numInGroup), out);

    int groups = ghDecoder.numGroups();
    interpretRow(
        wireFormat(bytes, ghDecoder.offset() + GroupSizeEncodingDecoder.numGroupsEncodingOffset(),
            GroupSizeEncodingDecoder.numGroupsEncodingLength()),
        0, "No. of groups", GroupSizeEncodingDecoder.numGroupsEncodingOffset(),
        GroupSizeEncodingDecoder.numGroupsEncodingLength(), String.format("%d", groups), out);

    int varData = ghDecoder.numVarDataFields();
    interpretRow(
        wireFormat(bytes,
            ghDecoder.offset() + GroupSizeEncodingDecoder.numVarDataFieldsEncodingOffset(),
            GroupSizeEncodingDecoder.numVarDataFieldsEncodingLength()),
        0, "No. of var data", GroupSizeEncodingDecoder.numVarDataFieldsEncodingOffset(),
        GroupSizeEncodingDecoder.numVarDataFieldsEncodingLength(), String.format("%d", varData), out);

    return ghDecoder.offset() + ghDecoder.encodedLength();
  }


  private int interpretMessageHeader(byte[] bytes, PrintStream out) {
    int blockLength = mhDecoder.blockLength();
    interpretRow(
        wireFormat(bytes, mhDecoder.offset() + MessageHeaderDecoder.blockLengthEncodingOffset(),
            MessageHeaderDecoder.blockLengthEncodingLength()),
        0, "SBE root block length", MessageHeaderDecoder.blockLengthEncodingOffset(),
        MessageHeaderDecoder.blockLengthEncodingLength(), String.format("%d", blockLength), out);
    int templateId = mhDecoder.templateId();
    interpretRow(
        wireFormat(bytes, mhDecoder.offset() + MessageHeaderDecoder.templateIdEncodingOffset(),
            MessageHeaderDecoder.templateIdEncodingLength()),
        0, "SBE template ID", MessageHeaderDecoder.templateIdEncodingOffset(),
        MessageHeaderDecoder.templateIdEncodingLength(), String.format("%d", templateId), out);
    int schemaId = mhDecoder.schemaId();
    interpretRow(
        wireFormat(bytes, mhDecoder.offset() + MessageHeaderDecoder.schemaIdEncodingOffset(),
            MessageHeaderDecoder.schemaIdEncodingLength()),
        0, "SBE schema ID", MessageHeaderDecoder.schemaIdEncodingOffset(),
        MessageHeaderDecoder.schemaIdEncodingLength(), String.format("%d", schemaId), out);
    int version = mhDecoder.version();
    interpretRow(
        wireFormat(bytes, mhDecoder.offset() + MessageHeaderDecoder.versionEncodingOffset(),
            MessageHeaderDecoder.versionEncodingLength()),
        0, "SBE schema version", MessageHeaderDecoder.versionEncodingOffset(),
        MessageHeaderDecoder.versionEncodingLength(), String.format("%d", version), out);

    int groups = mhDecoder.numGroups();
    interpretRow(
        wireFormat(bytes, mhDecoder.offset() + MessageHeaderDecoder.numGroupsEncodingOffset(),
            MessageHeaderDecoder.numGroupsEncodingLength()),
        0, "No. of groups", MessageHeaderDecoder.numGroupsEncodingOffset(),
        MessageHeaderDecoder.numGroupsEncodingLength(), String.format("%d", groups), out);

    int varData = mhDecoder.numVarDataFields();
    interpretRow(
        wireFormat(bytes,
            mhDecoder.offset() + MessageHeaderDecoder.numVarDataFieldsEncodingOffset(),
            MessageHeaderDecoder.numVarDataFieldsEncodingLength()),
        0, "No. of var data", MessageHeaderDecoder.numVarDataFieldsEncodingOffset(),
        MessageHeaderDecoder.numVarDataFieldsEncodingLength(), String.format("%d", varData), out);

    return mhDecoder.offset() + mhDecoder.encodedLength();
  }

  private void heading(String text, PrintStream out) {
    out.format("%s%s%s\n", headingBegin, text, headingEnd);
  }

  private void interpretRow(String wireFormat, int fieldId, String name, int offset, int length,
      String interpreted, PrintStream out) {
    out.format("%s %s%s%s %s %s %s %s %s %d %s %d %s %s %s\n", tableRowBegin, literalBegin,
        wireFormat, literalEnd, tableColumnDelim, fieldId > 0 ? Integer.toString(fieldId) : " ",
        tableColumnDelim, name, tableColumnDelim, offset, tableColumnDelim, length,
        tableColumnDelim, interpreted, tableRowEnd);
  }

  private void interpretTableHeader(PrintStream out) {
    out.format("%sWire format%sField ID%sName%sOffset%sLength%sInterpreted value%s\n",
        tableRowBegin, tableColumnDelim, tableColumnDelim, tableColumnDelim, tableColumnDelim,
        tableColumnDelim, tableRowEnd);
    out.format("%s-----------%s-------:%s----%s-----:%s-----:%s-----------------%s\n",
        tableRowBegin, tableColumnDelim, tableColumnDelim, tableColumnDelim, tableColumnDelim,
        tableColumnDelim, tableRowEnd);
  }

  private String monthYearToString(MONTH_YEARDecoder decoder) {
    StringBuilder sb = new StringBuilder();
    int year = decoder.year();
    if (year != MONTH_YEARDecoder.yearNullValue()) {
      sb.append("year=");
      sb.append(year);
      short month = decoder.month();
      sb.append(" month=");
      sb.append(month);
      short day = decoder.day();
      if (day != MONTH_YEARDecoder.dayNullValue()) {
        sb.append(" day=");
        sb.append(day);
      }
      short week = decoder.week();
      if (week != MONTH_YEARDecoder.weekNullValue()) {
        sb.append(" week=");
        sb.append(week);
      }
    } else {
      sb.append("null");
    }
    return sb.toString();
  }

}
