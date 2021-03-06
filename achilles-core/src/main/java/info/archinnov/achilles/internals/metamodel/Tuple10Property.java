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

package info.archinnov.achilles.internals.metamodel;

import static info.archinnov.achilles.internals.cql.TupleExtractor.extractType;
import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.GettableData;
import com.datastax.driver.core.TupleType;
import com.datastax.driver.core.TupleValue;
import com.google.common.reflect.TypeToken;

import info.archinnov.achilles.internals.metamodel.columns.FieldInfo;
import info.archinnov.achilles.type.tuples.Tuple10;
import info.archinnov.achilles.validation.Validator;

public class Tuple10Property<ENTITY, A, B, C, D, E, F, G, H, I, J> extends AbstractTupleProperty<ENTITY, Tuple10<A, B, C, D, E, F, G, H, I, J>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tuple10.class);

    private final AbstractProperty<ENTITY, A, ?> aProperty;
    private final AbstractProperty<ENTITY, B, ?> bProperty;
    private final AbstractProperty<ENTITY, C, ?> cProperty;
    private final AbstractProperty<ENTITY, D, ?> dProperty;
    private final AbstractProperty<ENTITY, E, ?> eProperty;
    private final AbstractProperty<ENTITY, F, ?> fProperty;
    private final AbstractProperty<ENTITY, G, ?> gProperty;
    private final AbstractProperty<ENTITY, H, ?> hProperty;
    private final AbstractProperty<ENTITY, I, ?> iProperty;
    private final AbstractProperty<ENTITY, J, ?> jProperty;

    public Tuple10Property(FieldInfo<ENTITY, Tuple10<A, B, C, D, E, F, G, H, I, J>> fieldInfo, AbstractProperty<ENTITY, A, ?> aProperty, AbstractProperty<ENTITY, B, ?> bProperty, AbstractProperty<ENTITY, C, ?> cProperty, AbstractProperty<ENTITY, D, ?> dProperty, AbstractProperty<ENTITY, E, ?> eProperty, AbstractProperty<ENTITY, F, ?> fProperty, AbstractProperty<ENTITY, G, ?> gProperty, AbstractProperty<ENTITY, H, ?> hProperty, AbstractProperty<ENTITY, I, ?> iProperty, AbstractProperty<ENTITY, J, ?> jProperty) {
        super(new TypeToken<Tuple10<A, B, C, D, E, F, G, H, I, J>>() {
        }, fieldInfo);
        this.aProperty = aProperty;
        this.bProperty = bProperty;
        this.cProperty = cProperty;
        this.dProperty = dProperty;
        this.eProperty = eProperty;
        this.fProperty = fProperty;
        this.gProperty = gProperty;
        this.hProperty = hProperty;
        this.iProperty = iProperty;
        this.jProperty = jProperty;
    }

    @Override
    TupleValue encodeFromJavaInternal(Tuple10<A, B, C, D, E, F, G, H, I, J> tuple10) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Encode from Java '%s' tuple10 %s to CQL type", fieldName, tuple10));
        }

        return tupleType.newValue(
                aProperty.encodeFromRaw(tuple10._1()),
                bProperty.encodeFromRaw(tuple10._2()),
                cProperty.encodeFromRaw(tuple10._3()),
                dProperty.encodeFromRaw(tuple10._4()),
                eProperty.encodeFromRaw(tuple10._5()),
                fProperty.encodeFromRaw(tuple10._6()),
                gProperty.encodeFromRaw(tuple10._7()),
                hProperty.encodeFromRaw(tuple10._8()),
                iProperty.encodeFromRaw(tuple10._9()),
                jProperty.encodeFromRaw(tuple10._10()));
    }

    @Override
    TupleValue encodeFromRawInternal(Object o) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Encode raw '%s' tuple10 object %s", fieldName, o));
        }

        Validator.validateTrue(Tuple10.class.isAssignableFrom(o.getClass()), "The class of object %s to encode should be Tuple10", o);
        return encodeFromJava((Tuple10<A, B, C, D, E, F, G, H, I, J>) o);
    }

    @Override
    Tuple10<A, B, C, D, E, F, G, H, I, J> decodeFromGettableInternal(GettableData gettableData) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Decode '%s' tuple10 from gettable object %s", fieldName, gettableData));
        }

        return decodeFromRaw(gettableData.getTupleValue(fieldInfo.quotedCqlColumn));
    }

    @Override
    Tuple10<A, B, C, D, E, F, G, H, I, J> decodeFromRawInternal(Object o) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Decode '%s' tuple10 raw object %s", fieldName, o));
        }

        Validator.validateTrue(TupleValue.class.isAssignableFrom(o.getClass()), "The class of object %s to decode should be %s", o, TupleValue.class.getCanonicalName());
        final List<DataType> types = tupleType.getComponentTypes();
        return new Tuple10<>(
                aProperty.decodeFromRaw(extractType((TupleValue) o, types.get(0), aProperty, 0)),
                bProperty.decodeFromRaw(extractType((TupleValue) o, types.get(1), bProperty, 1)),
                cProperty.decodeFromRaw(extractType((TupleValue) o, types.get(2), cProperty, 2)),
                dProperty.decodeFromRaw(extractType((TupleValue) o, types.get(3), dProperty, 3)),
                eProperty.decodeFromRaw(extractType((TupleValue) o, types.get(4), eProperty, 4)),
                fProperty.decodeFromRaw(extractType((TupleValue) o, types.get(5), fProperty, 5)),
                gProperty.decodeFromRaw(extractType((TupleValue) o, types.get(6), gProperty, 6)),
                hProperty.decodeFromRaw(extractType((TupleValue) o, types.get(7), hProperty, 7)),
                iProperty.decodeFromRaw(extractType((TupleValue) o, types.get(8), iProperty, 7)),
                jProperty.decodeFromRaw(extractType((TupleValue) o, types.get(9), jProperty, 9)));
    }

    @Override
    public TupleType buildType() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(format("Build current '%s' tuple10 data type", fieldName));
        }

        return tupleTypeFactory.typeFor(
                aProperty.buildType(),
                bProperty.buildType(),
                cProperty.buildType(),
                dProperty.buildType(),
                eProperty.buildType(),
                fProperty.buildType(),
                gProperty.buildType(),
                hProperty.buildType(),
                iProperty.buildType(),
                jProperty.buildType());
    }

    @Override
    protected List<AbstractProperty<ENTITY, ?, ?>> componentsProperty() {
        return Arrays.asList(aProperty, bProperty, cProperty, dProperty, eProperty, fProperty, gProperty, hProperty, iProperty, jProperty);
    }
}
