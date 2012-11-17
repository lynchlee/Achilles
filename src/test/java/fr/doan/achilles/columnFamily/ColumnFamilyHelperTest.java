package fr.doan.achilles.columnFamily;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import fr.doan.achilles.exception.InvalidColumnFamilyException;
import fr.doan.achilles.metadata.EntityMeta;
import fr.doan.achilles.metadata.PropertyMeta;
import fr.doan.achilles.metadata.SimplePropertyMeta;

@RunWith(MockitoJUnitRunner.class)
public class ColumnFamilyHelperTest
{

	// @InjectMocks
	ColumnFamilyHelper helper;

	@Mock
	Cluster cluster;

	@Mock
	Keyspace keyspace;

	@Mock
	KeyspaceDefinition keyspaceDefinition;

	@Mock
	private ColumnFamilyBuilder columnFamilyBuilder;

	@Mock
	private ColumnFamilyValidator columnFamilyValidator;

	private Map<Class<?>, EntityMeta<?>> entityMetaMap;

	private EntityMeta<?> meta;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		helper = new ColumnFamilyHelper(cluster, keyspace);
		ReflectionTestUtils.setField(helper, "columnFamilyBuilder", columnFamilyBuilder);
		ReflectionTestUtils.setField(helper, "columnFamilyValidator", columnFamilyValidator);
	}

	@Test
	public void should_discover_column_family() throws Exception
	{
		when(keyspace.getKeyspaceName()).thenReturn("keyspace");
		when(cluster.describeKeyspace("keyspace")).thenReturn(keyspaceDefinition);

		BasicColumnFamilyDefinition cfDef = new BasicColumnFamilyDefinition();
		cfDef.setName("testCF");
		cfDef.setKeyValidationClass("keyValidationClass");
		cfDef.setComment("comment");

		when(keyspaceDefinition.getCfDefs()).thenReturn(Arrays.asList((ColumnFamilyDefinition) cfDef));

		ColumnFamilyDefinition discoveredCfDef = helper.discoverColumnFamily("testCF");

		assertThat(discoveredCfDef).isNotNull();
		assertThat(discoveredCfDef.getName()).isEqualTo("testCF");
		assertThat(discoveredCfDef.getKeyValidationClass()).isEqualTo("keyValidationClass");
		assertThat(discoveredCfDef.getComment()).isEqualTo("comment");
	}

	@Test
	public void should_add_column_family() throws Exception
	{
		BasicColumnFamilyDefinition cfDef = new BasicColumnFamilyDefinition();

		when(cluster.addColumnFamily(cfDef, true)).thenReturn("id");

		String id = this.helper.addColumnFamily(cfDef);

		assertThat(id).isEqualTo("id");
	}

	@Test
	public void should_create_entity_meta() throws Exception
	{
		prepareData();
		BasicColumnFamilyDefinition cfDef = new BasicColumnFamilyDefinition();
		when(columnFamilyBuilder.build(meta, "keyspace")).thenReturn(cfDef);

		helper.createColumnFamily(meta);

		verify(columnFamilyBuilder).build(meta, "keyspace");
		verify(cluster).addColumnFamily(cfDef, true);

	}

	@Test
	public void should_validate_column_family() throws Exception
	{
		prepareData();
		BasicColumnFamilyDefinition cfDef = new BasicColumnFamilyDefinition();
		cfDef.setName("testCF");

		when(keyspaceDefinition.getCfDefs()).thenReturn(Arrays.asList((ColumnFamilyDefinition) cfDef));

		helper.validateColumnFamilies(entityMetaMap, true);
		verify(columnFamilyValidator).validate(cfDef, meta);
	}

	@Test
	public void should_validate_then_create_column_family_when_not_matching() throws Exception
	{
		prepareData();
		BasicColumnFamilyDefinition cfDef = new BasicColumnFamilyDefinition();
		cfDef.setName("testCF2");

		when(keyspaceDefinition.getCfDefs()).thenReturn(Arrays.asList((ColumnFamilyDefinition) cfDef));

		helper.validateColumnFamilies(entityMetaMap, true);
		verify(columnFamilyBuilder).build(meta, "keyspace");
	}

	@Test
	public void should_validate_then_create_column_family_when_null() throws Exception
	{
		prepareData();
		when(keyspaceDefinition.getCfDefs()).thenReturn(null);

		helper.validateColumnFamilies(entityMetaMap, true);
		verify(columnFamilyBuilder).build(meta, "keyspace");
	}

	@Test(expected = InvalidColumnFamilyException.class)
	public void should_exception_because_column_family_not_found() throws Exception
	{
		prepareData();
		when(keyspaceDefinition.getCfDefs()).thenReturn(null);

		helper.validateColumnFamilies(entityMetaMap, false);
	}

	private void prepareData()
	{
		Map<String, PropertyMeta<?>> map = new HashMap<String, PropertyMeta<?>>();

		SimplePropertyMeta<String> simplePropertyMeta = new SimplePropertyMeta<String>("name", String.class);
		map.put("name", simplePropertyMeta);
		meta = new EntityMeta<Long>(Long.class, "testCF", 1L, map);
		entityMetaMap = new HashMap<Class<?>, EntityMeta<?>>();
		entityMetaMap.put(this.getClass(), meta);

		when(keyspace.getKeyspaceName()).thenReturn("keyspace");
		when(cluster.describeKeyspace("keyspace")).thenReturn(keyspaceDefinition);
	}
}
