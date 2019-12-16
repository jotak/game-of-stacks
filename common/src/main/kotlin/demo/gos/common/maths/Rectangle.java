/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.gos.common.maths;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joel Takvorian
 */
public final class Rectangle {
  private final double top;
  private final double left;
  private final double bottom;
  private final double right;

  public Rectangle(double top, double left, double bottom, double right) {
    this.top = top;
    this.left = left;
    this.bottom = bottom;
    this.right = right;
  }

  public double top() {
    return top;
  }

  public double left() {
    return left;
  }

  public double bottom() {
    return bottom;
  }

  public double right() {
    return right;
  }

  public Point middle() {
    return new Segment(new Point(left, top), new Point(right, bottom)).middle();
  }

  @Override
  public String toString() {
    return "Rectangle{" +
        "top=" + top +
        ", left=" + left +
        ", bottom=" + bottom +
        ", right=" + right +
        '}';
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>(4);
    map.put("top", top);
    map.put("left", left);
    map.put("bottom", bottom);
    map.put("right", right);
    return map;
  }

  public static Rectangle fromMap(Map<String, Object> map) {
    double top = (double) map.get("top");
    double left = (double) map.get("left");
    double bottom = (double) map.get("bottom");
    double right = (double) map.get("right");
    return new Rectangle(top, left, bottom, right);
  }
}
