/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.encrypt.rewrite.parameterized;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sharding.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.sharding.core.yaml.swapper.root.RuleRootConfigurationsYamlSwapper;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptSQLRewriterParameterizedTest extends AbstractSQLRewriterParameterizedTest {
    
    private static final String PATH = "encrypt";
    
    public EncryptSQLRewriterParameterizedTest(final String type, final String name, final String fileName, final SQLRewriteEngineTestParameters testParameters) {
        super(testParameters);
    }
    
    @Parameters(name = "{0}: {1} -> {2}")
    public static Collection<Object[]> loadTestParameters() {
        return SQLRewriteEngineTestParametersBuilder.loadTestParameters(PATH.toUpperCase(), PATH, EncryptSQLRewriterParameterizedTest.class);
    }
    
    @Override
    protected Collection<SQLRewriteUnit> createSQLRewriteUnits() throws IOException {
        YamlRootRuleConfigurations ruleConfigurations = createRuleConfigurations();
        Collection<ShardingSphereRule> rules = ShardingSphereRulesBuilder.build(new RuleRootConfigurationsYamlSwapper().swap(ruleConfigurations), ruleConfigurations.getDataSources().keySet());
        SQLParserEngine sqlParserEngine = SQLParserEngineFactory.getSQLParserEngine(null == getTestParameters().getDatabaseType() ? "SQL92" : getTestParameters().getDatabaseType());
        ShardingSphereMetaData metaData = createShardingSphereMetaData();
        ConfigurationProperties properties = new ConfigurationProperties(ruleConfigurations.getProps());
        RouteContext routeContext = new DataNodeRouter(metaData, properties, rules).route(
                sqlParserEngine.parse(getTestParameters().getInputSQL(), false), getTestParameters().getInputSQL(), getTestParameters().getInputParameters());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(metaData.getSchema().getConfiguredSchemaMetaData(),
                properties, rules).rewrite(getTestParameters().getInputSQL(), getTestParameters().getInputParameters(), routeContext);
        return sqlRewriteResult instanceof GenericSQLRewriteResult
                ? Collections.singletonList(((GenericSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnit()) : (((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits()).values();
    }
    
    private YamlRootRuleConfigurations createRuleConfigurations() throws IOException {
        URL url = EncryptSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootRuleConfigurations.class);
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount", "status"));
        when(schemaMetaData.getAllColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount", "status"));
        RuleSchemaMetaData ruleSchemaMetaData = mock(RuleSchemaMetaData.class);
        when(ruleSchemaMetaData.getConfiguredSchemaMetaData()).thenReturn(schemaMetaData);
        when(ruleSchemaMetaData.getSchemaMetaData()).thenReturn(schemaMetaData);
        return new ShardingSphereMetaData(mock(DataSourceMetas.class), ruleSchemaMetaData);
    }
}
