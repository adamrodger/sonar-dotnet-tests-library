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
import org.mockito.Mockito;
import org.sonar.api.config.Settings;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnitTestResultsAggregatorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void hasUnitTestResultsProperty() {
    Settings settings = mock(Settings.class);

    UnitTestConfiguration unitTestConf = new UnitTestConfiguration("visualStudioTestResultsFile", "nunitTestResultsFile");

    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(false);
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(false);
    assertThat(new UnitTestResultsAggregator(unitTestConf, settings).hasUnitTestResultsProperty()).isFalse();

    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(false);
    assertThat(new UnitTestResultsAggregator(unitTestConf, settings).hasUnitTestResultsProperty()).isTrue();

    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(false);
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(true);
    assertThat(new UnitTestResultsAggregator(unitTestConf, settings).hasUnitTestResultsProperty()).isTrue();

    unitTestConf = new UnitTestConfiguration("visualStudioTestResultsFile2", "nunit2");
    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(true);
    assertThat(new UnitTestResultsAggregator(unitTestConf, settings).hasUnitTestResultsProperty()).isFalse();
  }

  @Test
  public void aggregate() {
    UnitTestConfiguration unitTestConf = new UnitTestConfiguration("visualStudioTestResultsFile", "nunitTestResultsFile");
    Settings settings = mock(Settings.class);

    // Visual Studio test results only
    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    when(settings.getString("visualStudioTestResultsFile")).thenReturn("foo.trx");
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(false);
    VisualStudioTestResultsFileParser visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    NUnitTestResultsFileParser nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    UnitTestResults results = mock(UnitTestResults.class);
    new UnitTestResultsAggregator(unitTestConf, settings, visualStudioTestResultsFileParser, nunitTestResultsFileParser).aggregate(results);
    verify(visualStudioTestResultsFileParser).parse(new File("foo.trx"), results);
    verify(nunitTestResultsFileParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(UnitTestResults.class));

    // NUnit test results only
    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(false);
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(true);
    when(settings.getString("nunitTestResultsFile")).thenReturn("foo.xml");
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new UnitTestResultsAggregator(unitTestConf, settings, visualStudioTestResultsFileParser, nunitTestResultsFileParser).aggregate(results);
    verify(visualStudioTestResultsFileParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(UnitTestResults.class));
    verify(nunitTestResultsFileParser).parse(new File("foo.xml"), results);

    // Both Visual Studio and NUnit configured
    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    when(settings.getString("visualStudioTestResultsFile")).thenReturn("foo.trx");
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(true);
    when(settings.getString("nunitTestResultsFile")).thenReturn("foo.xml");
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new UnitTestResultsAggregator(unitTestConf, settings, visualStudioTestResultsFileParser, nunitTestResultsFileParser).aggregate(results);
    verify(visualStudioTestResultsFileParser).parse(new File("foo.trx"), results);
    verify(nunitTestResultsFileParser).parse(new File("foo.xml"), results);

    // Visual Studio and NUnit not configured
    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(false);
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(false);
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new UnitTestResultsAggregator(unitTestConf, settings, visualStudioTestResultsFileParser, nunitTestResultsFileParser).aggregate(results);
    verify(visualStudioTestResultsFileParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(UnitTestResults.class));
    verify(nunitTestResultsFileParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(UnitTestResults.class));

    // Multiple files configured
    when(settings.hasKey("visualStudioTestResultsFile")).thenReturn(true);
    when(settings.getString("visualStudioTestResultsFile")).thenReturn(",foo.trx  ,bar.trx");
    when(settings.hasKey("nunitTestResultsFile")).thenReturn(false);
    visualStudioTestResultsFileParser = mock(VisualStudioTestResultsFileParser.class);
    nunitTestResultsFileParser = mock(NUnitTestResultsFileParser.class);
    results = mock(UnitTestResults.class);
    new UnitTestResultsAggregator(unitTestConf, settings, visualStudioTestResultsFileParser, nunitTestResultsFileParser).aggregate(results);
    verify(visualStudioTestResultsFileParser).parse(new File("foo.trx"), results);
    verify(visualStudioTestResultsFileParser).parse(new File("bar.trx"), results);
    verify(nunitTestResultsFileParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(UnitTestResults.class));
  }

}
