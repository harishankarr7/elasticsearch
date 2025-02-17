/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.ql.util;

import org.elasticsearch.common.geo.SpatialPoint;
import org.elasticsearch.test.ESTestCase;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.closeTo;

public class SpatialCoordinateTypesTests extends ESTestCase {

    private static final Map<SpatialCoordinateTypes, TestTypeFunctions> types = new LinkedHashMap<>();
    static {
        types.put(SpatialCoordinateTypes.GEO, new TestTypeFunctions(ESTestCase::randomGeoPoint, v -> 1e-5));
        types.put(SpatialCoordinateTypes.CARTESIAN, new TestTypeFunctions(ESTestCase::randomCartesianPoint, v -> Math.abs(v / 1e5)));
    }

    record TestTypeFunctions(Supplier<SpatialPoint> randomPoint, Function<Double, Double> error) {}

    public void testEncoding() {
        for (var type : types.entrySet()) {
            for (int i = 0; i < 10; i++) {
                SpatialCoordinateTypes coordType = type.getKey();
                SpatialPoint original = type.getValue().randomPoint().get();
                var error = type.getValue().error;
                SpatialPoint point = coordType.longAsPoint(coordType.pointAsLong(original));
                assertThat(coordType + ": Y[" + i + "]", point.getY(), closeTo(original.getY(), error.apply(original.getX())));
                assertThat(coordType + ": X[" + i + "]", point.getX(), closeTo(original.getX(), error.apply(original.getY())));
            }
        }
    }

    public void testParsing() {
        for (var type : types.entrySet()) {
            for (int i = 0; i < 10; i++) {
                SpatialCoordinateTypes coordType = type.getKey();
                SpatialPoint geoPoint = type.getValue().randomPoint.get();
                SpatialPoint point = coordType.stringAsPoint(coordType.pointAsString(geoPoint));
                assertThat(coordType + ": Y[" + i + "]", point.getY(), closeTo(geoPoint.getY(), 1e-5));
                assertThat(coordType + ": X[" + i + "]", point.getX(), closeTo(geoPoint.getX(), 1e-5));
            }
        }
    }
}
