/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.rel.rules;

import org.apache.calcite.adapter.enumerable.EnumerableAggregate;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableProject;
import org.apache.calcite.adapters.enumerable.MyRemovableEnumerableProject;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.logical.LogicalAggregate;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.sql.SqlKind;

/**
 * Rule to convert a {@link LogicalAggregate}
 * to an {@link EnumerableAggregate}.
 */
public class MyRemovableEnumerableProjectRule extends ConverterRule {
  static final Config DEFAULT_CONFIG;

  static {
    DEFAULT_CONFIG = Config.INSTANCE.as(Config.class).withConversion(LogicalProject.class, (p) -> {
      return !p.containsOver();
    }, Convention.NONE, EnumerableConvention.INSTANCE, "MyRemovableEnumerableProjectRule").withRuleFactory(MyRemovableEnumerableProjectRule::new);
  }

  public static final MyRemovableEnumerableProjectRule INSTANCE = DEFAULT_CONFIG.toRule(MyRemovableEnumerableProjectRule.class);//new MyEnumerableAggregateRule();

  protected MyRemovableEnumerableProjectRule(Config config) {
    super(config);
  }

  public RelNode convert(RelNode rel) {
    Project project = (Project)rel;
    if(isRemovable(project)) {
      return MyRemovableEnumerableProject.create(convert(project.getInput(), project.getInput().getTraitSet().replace(EnumerableConvention.INSTANCE)), project.getProjects(), project.getRowType());
    } else {
      return EnumerableProject.create(convert(project.getInput(), project.getInput().getTraitSet().replace(EnumerableConvention.INSTANCE)), project.getProjects(), project.getRowType());
    }
  }

  private boolean isRemovable(Project project) {
    return project.getProjects().stream().allMatch(expr -> expr.getKind() == SqlKind.INPUT_REF);
  }


}
