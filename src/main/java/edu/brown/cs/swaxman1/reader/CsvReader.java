package edu.brown.cs.swaxman1.reader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class for reading CSV files and splitting their lines into records.
 *
 * @author swaxman1
 *
 */
public class CsvReader {

  /**
   * Constructor for CsvReader.
   *
   * @param path - path to file to read
   * @param delim - delimeter to split on
   * @throws InvalidFormatException - Thrown if file has lines with different
   *           numbers of fields
   * @throws IOException - Thrown if reader fails or closing a file fails
   */
  public CsvReader(String path, String delim) throws InvalidFormatException,
          IOException {
    this.path = path;
    this.delim = delim;
    this.records = readLines();
  }

  /**
   * Same as other constructor but assumes delimeter is comma.
   *
   * @param path - path to file to read
   * @throws InvalidFormatException - Thrown if file has lines with different
   *           numbers of fields
   * @throws IOException - Thrown if reader fails or closing a file fails
   */
  public CsvReader(String path) throws IOException, InvalidFormatException {
    this.path = path;
    this.delim = ",";
    this.records = readLines();
  }

  // path the file to read
  private String path;
  // the records indicated by each line of csv
  private String[][] records;
  // the delimeter to split on
  private String delim;

  /**
   * Parses the file into records.
   *
   * @return a string[][] where each row is a record
   * @throws IOException - Thrown if reader fails or closing a file fails
   * @throws InvalidFormatException - thron if file has lines with different
   *           numbers of fields
   */
  private String[][] readLines() throws IOException, InvalidFormatException {
    BufferedReader r;
    try {
      r = new BufferedReader(new FileReader(this.path));
    } catch (FileNotFoundException e) {
      throw new FileNotFoundException("CsvReader: " + e.getMessage());
    }
    ArrayList<String> lines = new ArrayList<String>();
    try {
      // Go through lines, adding them to a list
      String line = r.readLine();
      while (line != null) {
        lines.add(line);
        line = r.readLine();
      }
      String[][] tempRecords = new String[lines.size()][];
      // for each line, split it by the delim and set the rows of an array
      // to these records
      for (int i = 0; i < lines.size(); i++) {
        tempRecords[i] = lines.get(i).split(this.delim, -1);
      }
      if (tempRecords.length == 0) {
        r.close();
        return tempRecords;
      }
      // check that all records have same field numbers
      r.close();
      return tempRecords;
    } catch (IOException e) {
      throw new IOException("CsvReader: " + e.getMessage());
    }
  }

  /**
   * Gets record on specified line number.
   *
   * @param line - line number
   * @return the record from that line of the csv file
   */
  public String[] getRecord(int line) {
    // catch invalid line numbers
    if (line < 0 || line >= this.records.length) {
      throw new IndexOutOfBoundsException(
              "The line number you entered did not exist in the "
                      + "specified file.");
    }
    // clone so the underlying recordscan't be altered
    return this.records[line].clone();
  }

  /**
   * Getter for the delimeter.
   *
   * @return the delimeter
   */
  public String getDelim() {
    return this.delim;
  }

  /**
   * Getter for the path name.
   *
   * @return the path name
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Getter for the records created by the reader.
   *
   * @return the records created by the reader
   */
  public String[][] getRecords() {
    String[][] copy = new String[this.records.length][];
    for (int i = 0; i < this.records.length; i++) {
      copy[i] = this.records[i].clone();
    }
    return copy;
  }

  @Override
  public String toString() {
    return "CSVReader for " + this.path;
  }
}
