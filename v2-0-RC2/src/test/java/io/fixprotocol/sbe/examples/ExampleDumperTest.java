package io.fixprotocol.sbe.examples;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import org.junit.jupiter.api.Test;

class ExampleDumperTest {

  @Test
  void testMain() throws UnsupportedEncodingException, FileNotFoundException {
    File outputDir = new File("doc");
    outputDir.mkdir();
    File output = new File(outputDir, "examples.md");
    ExampleDumper.main(new String[] {output.toString()});
    assertTrue(output.exists());
  }

}
