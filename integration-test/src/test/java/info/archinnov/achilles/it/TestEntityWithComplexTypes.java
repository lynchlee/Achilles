/*
 * Copyright (C) 2012-2016 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.archinnov.achilles.it;

import static com.datastax.driver.core.ConsistencyLevel.*;
import static com.datastax.driver.core.DataType.*;
import static info.archinnov.achilles.embedded.CassandraEmbeddedConfigParameters.DEFAULT_CASSANDRA_EMBEDDED_KEYSPACE_NAME;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.generated.manager.EntityWithComplexTypes_Manager;
import info.archinnov.achilles.internals.types.ClassAnnotatedByCodec;
import info.archinnov.achilles.internals.entities.EntityWithComplexTypes;
import info.archinnov.achilles.internals.types.IntWrapper;
import info.archinnov.achilles.internals.entities.TestUDT;
import info.archinnov.achilles.junit.AchillesTestResource;
import info.archinnov.achilles.junit.AchillesTestResourceBuilder;
import info.archinnov.achilles.script.ScriptExecutor;
import info.archinnov.achilles.type.tuples.Tuple1;
import info.archinnov.achilles.type.tuples.Tuple2;
import info.archinnov.achilles.type.tuples.Tuple3;

@RunWith(MockitoJUnitRunner.class)
public class TestEntityWithComplexTypes {

    @Rule
    public AchillesTestResource<ManagerFactory> resource = AchillesTestResourceBuilder
            .forJunit()
            .entityClassesToTruncate(EntityWithComplexTypes.class)
            .truncateBeforeAndAfterTest()
            .build((cluster, statementsCache) -> ManagerFactoryBuilder
                    .builder(cluster)
                    .withManagedEntityClasses(EntityWithComplexTypes.class)
                    .doForceSchemaCreation(true)
                    .withStatementsCache(statementsCache)
                    .withDefaultKeyspaceName(DEFAULT_CASSANDRA_EMBEDDED_KEYSPACE_NAME)
                    .build());

    private Session session = resource.getNativeSession();
    private ScriptExecutor scriptExecutor = resource.getScriptExecutor();
    private EntityWithComplexTypes_Manager manager = resource.getManagerFactory().forEntityWithComplexTypes();

    @Test
    public void should_insert() throws Exception {
        //Given
        final long id = RandomUtils.nextLong(0L, Long.MAX_VALUE);
        final TestUDT udt = new TestUDT();
        udt.setList(asList("list"));
        udt.setName("name");
        udt.setMap(ImmutableMap.of(1, "1"));
        UUID timeuuid = UUIDs.timeBased();


        final EntityWithComplexTypes entity = new EntityWithComplexTypes();
        entity.setId(id);
        entity.setCodecOnClass(new ClassAnnotatedByCodec());
        entity.setComplexNestingMap(ImmutableMap.of(udt,
                ImmutableMap.of(1, Tuple3.of(1, 2, ConsistencyLevel.ALL))));
        entity.setConsistencyLevel(ConsistencyLevel.EACH_QUORUM);
        entity.setInteger(123);
        entity.setJsonMap(ImmutableMap.of(1, asList(1, 2, 3)));
        entity.setListNesting(asList(ImmutableMap.of(1, "one")));
        entity.setListUdt(asList(udt));
        entity.setMapUdt(ImmutableMap.of(1, udt));
        entity.setMapWithNestedJson(ImmutableMap.of(1, asList(ImmutableMap.of(1, "one"))));
        entity.setObjectBoolean(new Boolean(true));
        entity.setObjectByte(new Byte("5"));
        entity.setObjectByteArray(new Byte[]{7});
        entity.setOkSet(Sets.newHashSet(ConsistencyLevel.LOCAL_ONE, ConsistencyLevel.LOCAL_QUORUM));
        entity.setPrimitiveBoolean(true);
        entity.setPrimitiveByte((byte) 3);
        entity.setPrimitiveByteArray(new byte[]{4});
        entity.setSimpleUdt(udt);
        entity.setTime(buildDate());
        entity.setTimeuuid(timeuuid);
        entity.setTuple1(Tuple1.of(ConsistencyLevel.THREE));
        entity.setTuple2(Tuple2.of(ConsistencyLevel.TWO, 2));
        entity.setTupleNesting(Tuple2.of(1, asList("1")));
        entity.setValue("val");
        entity.setWriteTime(1000L);
        entity.setWriteTimeWithCodec("2000");
        entity.setIntWrapper(new IntWrapper(123));

        //When
        manager
                .crud()
                .insert(entity)
                .execute();

        //Then
        final Metadata metadata = session.getCluster().getMetadata();
        final TupleValue tupleValue = metadata.newTupleType(text(), cint(), cint())
                .newValue("1", 2, 5);

        final TupleValue nestedTuple2Value = metadata.newTupleType(cint(), list(text())).newValue(1, asList("1"));

        final Row actual = session.execute("SELECT * FROM entity_complex_types WHERE id = " + id).one();

        assertThat(actual).isNotNull();
        assertThat(actual.getString("codec_on_class")).isEqualTo("ClassAnnotatedByCodec{}");
        final Map<String, Map<Integer, TupleValue>> complexMapNesting = actual.getMap("complex_nesting_map", new TypeToken<String>() {
                },
                new TypeToken<Map<Integer, TupleValue>>() {
                });
        assertThat(complexMapNesting).containsEntry("{\"list\":[\"list\"],\"map\":{\"1\":\"1\"},\"name\":\"name\"}",
                ImmutableMap.of(1, tupleValue));
        assertThat(actual.getString("consistencylevel")).isEqualTo("EACH_QUORUM");
        assertThat(actual.getString("integer")).isEqualTo("123");
        assertThat(actual.getString("json_map")).isEqualTo("{\"1\":[1,2,3]}");
        assertThat(actual.getList("list_nesting", new TypeToken<Map<Integer, String>>() {
        }))
                .containsExactly(ImmutableMap.of(1, "one"));
        final UDTValue foundUDT = actual.getUDTValue("simple_udt");
        assertThat(foundUDT.getString("name")).isEqualTo("name");
        assertThat(foundUDT.getList("list", String.class)).containsExactly("list");
        assertThat(foundUDT.getMap("map", String.class, String.class)).containsEntry("1", "1");
        assertThat(actual.getList("list_udt", UDTValue.class)).containsExactly(foundUDT);
        assertThat(actual.getMap("map_udt", Integer.class, UDTValue.class)).containsEntry(1, foundUDT);
        assertThat(actual.getMap("map_with_nested_json", Integer.class, String.class))
                .containsEntry(1, "[{\"1\":\"one\"}]");
        assertThat(actual.getBool("object_bool")).isTrue();
        assertThat(actual.getByte("object_byte")).isEqualTo((byte) 5);
        assertThat(actual.getBytes("object_byte_array")).isEqualTo(ByteBuffer.wrap(new byte[]{(byte) 7}));
        assertThat(actual.getSet("ok_set", Integer.class)).containsExactly(6, 10);
        assertThat(actual.getBool("primitive_bool")).isTrue();
        assertThat(actual.getByte("primitive_byte")).isEqualTo((byte) 3);
        assertThat(actual.getBytes("primitive_byte_array")).isEqualTo(ByteBuffer.wrap(new byte[]{(byte) 4}));
        assertThat(actual.getString("time")).isEqualTo(buildDate().getTime() + "");
        assertThat(actual.getUUID("timeuuid")).isEqualTo(timeuuid);
        assertThat(actual.getTupleValue("tuple1").get(0, String.class)).isEqualTo("\"THREE\"");
        assertThat(actual.getTupleValue("tuple2").get(0, String.class)).isEqualTo("\"TWO\"");
        assertThat(actual.getTupleValue("tuple2").get(1, String.class)).isEqualTo("2");
        assertThat(actual.getTupleValue("tuple_nesting")).isEqualTo(nestedTuple2Value);
        assertThat(actual.getString("value")).isEqualTo("val");
        assertThat(actual.getInt("intwrapper")).isEqualTo(123);
    }

    @Test
    public void should_find() throws Exception {
        //Given
        final long id = RandomUtils.nextLong(0L, Long.MAX_VALUE);
        scriptExecutor.executeScriptTemplate("EntityWithComplexTypes/insert_single_row.cql", ImmutableMap.of("id", id));

        final TestUDT udt = new TestUDT();
        udt.setList(asList("list"));
        udt.setName("name");
        udt.setMap(ImmutableMap.of(1, "1"));

        //When
        final EntityWithComplexTypes actual = manager
                .crud()
                .findById(id)
                .get();

        //Then
        assertThat(actual.getCodecOnClass()).isInstanceOf(ClassAnnotatedByCodec.class);
        assertThat(actual.getComplexNestingMap()).containsEntry(udt,
                ImmutableMap.of(1, Tuple3.of(1, 2, ConsistencyLevel.ALL)));
        assertThat(actual.getConsistencyLevel()).isEqualTo(EACH_QUORUM);
        assertThat(actual.getInteger()).isEqualTo(123);
        assertThat(actual.getJsonMap()).containsEntry(1, asList(1, 2, 3));
        assertThat(actual.getListNesting()).containsExactly(ImmutableMap.of(1, "one"));
        assertThat(actual.getListUdt()).containsExactly(udt);
        assertThat(actual.getMapUdt()).containsEntry(1, udt);
        assertThat(actual.getMapWithNestedJson()).containsEntry(1, asList(ImmutableMap.of(1, "one")));
        assertThat(actual.getObjectBoolean()).isTrue();
        assertThat(actual.getObjectByte()).isEqualTo((byte) 5);
        assertThat(actual.getObjectByteArray()).isEqualTo(new Byte[]{0, 0, 0, 0, 0, 0, 0, 7});
        assertThat(actual.getOkSet()).containsOnly(LOCAL_ONE, LOCAL_QUORUM);
        assertThat(actual.isPrimitiveBoolean()).isTrue();
        assertThat(actual.getPrimitiveByte()).isEqualTo((byte) 3);
        assertThat(actual.getPrimitiveByteArray()).isEqualTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 4});
        assertThat(actual.getSimpleUdt()).isEqualTo(udt);
        assertThat(actual.getTime()).isEqualTo(new Date(1234567));
        assertThat(actual.getTimeuuid()).isEqualTo(UUID.fromString("50554d6e-29bb-11e5-b345-feff819cdc9f"));
        assertThat(actual.getTuple1()).isEqualTo(Tuple1.of(THREE));
        assertThat(actual.getTuple2()).isEqualTo(Tuple2.of(TWO, 2));
        assertThat(actual.getTupleNesting()).isEqualTo(Tuple2.of(1, asList("1")));
        assertThat(actual.getValue()).isEqualTo("val");
        assertThat(actual.getWriteTime()).isGreaterThan(0L);
        assertThat(actual.getWriteTimeWithCodec()).isNotNull();
        assertThat(actual.getIntWrapper()).isEqualTo(new IntWrapper(456));
    }

    @Test
    public void should_dsl_select() throws Exception {
        //Given
        final long id = RandomUtils.nextLong(0L, Long.MAX_VALUE);
        scriptExecutor.executeScriptTemplate("EntityWithComplexTypes/insert_single_row.cql", ImmutableMap.of("id", id));

        //When
        final EntityWithComplexTypes actual = manager
                .dsl()
                .select()
                .writeTime()
                .writeTimeWithCodec()
                .fromBaseTable()
                .where()
                .id_Eq(id)
                .getOne();

        //Then
        assertThat(actual).isNotNull();
        assertThat(actual.getWriteTime()).isGreaterThan(0L);
        assertThat(actual.getWriteTimeWithCodec()).isNotEmpty();
    }

    private Date buildDate() throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.parse("2015-10-01 00:00:00 GMT");
    }
}