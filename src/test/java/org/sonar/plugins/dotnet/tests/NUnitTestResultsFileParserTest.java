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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class NUnitTestResultsFileParserTest {
  private static final String NO_COUNTERS_FILE = "src/test/resources/nunit/no_counters.xml";
  private static final String INVALID_COUNTER_FILE = "src/test/resources/nunit/invalid_total.xml";
  private static final String VALID_FILE = "src/test/resources/nunit/valid.xml";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void no_counters() {
    thrown.expect(ParseErrorException.class);
    thrown.expectMessage("Missing attribute \"total\" in element <test-results> in ");
    thrown.expectMessage(new File(NO_COUNTERS_FILE).getAbsolutePath());
    new NUnitTestResultsFileParser().parse(new File(NO_COUNTERS_FILE), mock(UnitTestResults.class));
  }

  @Test
  public void wrong_passed_number() {
    thrown.expect(ParseErrorException.class);
    thrown.expectMessage("Expected an integer instead of \"invalid\" for the attribute \"total\" in ");
    thrown.expectMessage(new File(INVALID_COUNTER_FILE).getAbsolutePath());
    new NUnitTestResultsFileParser().parse(new File(INVALID_COUNTER_FILE), mock(UnitTestResults.class));
  }

  @Test
  public void valid() throws Exception {
    UnitTestResults results = new UnitTestResults();
    new NUnitTestResultsFileParser().parse(new File(VALID_FILE), results);

    assertThat(results.tests()).isEqualTo(200);
    assertThat(results.passedPercentage()).isEqualTo(67.5); // 135/200 tests succeeded
    assertThat(results.skipped()).isEqualTo(14);
    assertThat(results.failed()).isEqualTo(20);
    assertThat(results.errors()).isEqualTo(31);
  }
}
