/*
 * SonarQube .NET Tests Library
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.dotnet.tests;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * NUnit Test Results File Parser
 */
public class NUnitTestResultsFileParser implements UnitTestParser {

  private static final Logger LOG = LoggerFactory.getLogger(NUnitTestResultsFileParser.class);

  public void parse(File file, UnitTestResults unitTestResults) {
	LOG.info("Parsing the NUnit Test Results file " + file.getAbsolutePath());
	new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private XmlParserHelper xmlParserHelper;
    private final UnitTestResults unitTestResults;

    private boolean foundCounters;

    public Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    public void parse() {
      try {
        xmlParserHelper = new XmlParserHelper(file);
        checkTestResultsTag();
        handleTestResultsTag();
        Preconditions.checkArgument(foundCounters, "The mandatory <test-results> tag is missing in " + file.getAbsolutePath());
      } finally {
        if (xmlParserHelper != null) {
          xmlParserHelper.close();
        }
      }
    }

    private void checkTestResultsTag() {
      xmlParserHelper.checkRootTag("test-results");
      foundCounters = true;
    }

    private void handleTestResultsTag() {
      int total = xmlParserHelper.getRequiredIntAttribute("total");
      int errors = xmlParserHelper.getRequiredIntAttribute("errors");
      int failures = xmlParserHelper.getRequiredIntAttribute("failures");
      int notRun = xmlParserHelper.getRequiredIntAttribute("not-run");
      int inconclusive = xmlParserHelper.getRequiredIntAttribute("inconclusive");
      int ignored = xmlParserHelper.getRequiredIntAttribute("ignored");
      int skipped = xmlParserHelper.getRequiredIntAttribute("skipped");
      int invalid = xmlParserHelper.getRequiredIntAttribute("invalid");

      int passed = total - errors - failures - notRun - inconclusive - ignored - skipped - invalid;

      unitTestResults.add(total, passed, notRun + ignored + skipped + inconclusive, failures, errors + invalid);
    }
  }
}
