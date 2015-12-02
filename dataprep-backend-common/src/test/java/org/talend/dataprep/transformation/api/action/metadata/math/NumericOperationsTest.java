package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Unit test for the NumericOperations action.
 * 
 * @see NumericOperations
 */
public class NumericOperationsTest {

    /** The action to test. */
    private NumericOperations action;

    /** The action parameters. */
    private Map<String, String> parameters;

    /**
     * Constructor.
     */
    public NumericOperationsTest() throws IOException {
        action = new NumericOperations();
    }

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = NumericOperationsTest.class.getResourceAsStream("numericOpsAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.MATH.getDisplayName()));
    }

    @Test
    public void testCompute() {
        // valids:
        assertEquals("5", action.compute("3", "+", "2"));
        assertEquals("6", action.compute("3", "x", "2"));
        assertEquals("1", action.compute("3", "-", "2"));
        assertEquals("2", action.compute("4", "/", "2"));

        // silently fail when divide by 0
        assertEquals("", action.compute("3", "/", "0"));

        // empty items:
        assertEquals("", action.compute("", "/", "2"));
        assertEquals("", action.compute("3", "", "2"));
        assertEquals("", action.compute("3", "/", ""));

        // invalid items:
        assertEquals("", action.compute("a", "/", "2"));
        assertEquals("", action.compute("3", "a", "2"));
        assertEquals("", action.compute("3", "/", "a"));

        // null items:
        assertEquals("", action.compute(null, "/", "2"));
        assertEquals("", action.compute("3", null, "2"));
        assertEquals("", action.compute("3", "/", null));
    }

    @Test
    public void testComputeScaleAndRoundMultiply() {
        assertEquals("6.66", action.compute("3.33", "x", "2"));
        assertEquals("36.22", action.compute("18.1111", "x", "2"));
    }

    @Test
    public void testComputeScaleAndRoundDivide() {
        assertEquals("1.5", action.compute("3", "/", "2"));
        assertEquals("2", action.compute("18", "/", "9"));
        assertEquals("2.11", action.compute("19", "/", "9"));
        assertEquals("211.11", action.compute("1900", "/", "9"));
        assertEquals("21111.11", action.compute("190000", "/", "9"));
    }

    @Test
    public void testComputeScaleAndRoundAdd() {
        assertEquals("5.33", action.compute("3.33", "+", "2"));
        assertEquals("20.11", action.compute("18.1111", "+", "2"));
    }

    @Test
    public void testComputeScaleAndRoundSubstract() {
        assertEquals("1.33", action.compute("3.33", "-", "2"));
        assertEquals("16.11", action.compute("18.1111", "-", "2"));
    }

    @Test
    public void testComputeDecimalOperand() {
        assertEquals("5.1", action.compute("3", "+", "2.1"));
        assertEquals("6.3", action.compute("3", "x", "2.1"));
        assertEquals("0.9", action.compute("3", "-", "2.1"));
        assertEquals("1.9", action.compute("4", "/", "2.1"));
    }


    @Test
    public void testComputeScientificOperand() {
        assertEquals("2400", action.compute("1.2E3", "x", "2"));
        assertEquals("2400", action.compute("2", "x", "1.2E3"));
        assertEquals("2640", action.compute("2.2", "x", "1.2E3"));
    }

    @Test
    public void should_apply_on_column() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        // when
        action.applyOnColumn(row, new ActionContext(new TransformationContext(), row.getRowMetadata()), parameters, "0000");

        // then
        DataSetRow expected = getRow("5", "3", "Done !", "8");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_created_column() {
        // given
        DataSetRow row = getRow("5", "3");

        // when
        final ActionContext context = new ActionContext(new TransformationContext(), row.getRowMetadata());
        new NumericOperations().applyOnColumn(row, context, parameters, "0000");
        final ActionContext context2 = new ActionContext(new TransformationContext(), row.getRowMetadata());
        new NumericOperations().applyOnColumn(row, context2, parameters, "0002");

        // then
        DataSetRow expected = getRow("5", "3", "8", "11");
        assertEquals(expected, row);
    }


    @Test
    public void should_apply_on_column_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.remove(NumericOperations.SELECTED_COLUMN_PARAMETER);
        parameters.put(NumericOperations.MODE_PARAMETER, NumericOperations.CONSTANT_MODE);

        // when
        action.applyOnColumn(row, new ActionContext(new TransformationContext(), row.getRowMetadata()), parameters, "0000");

        // then
        DataSetRow expected = getRow("5", "3", "Done !", "7");
        assertEquals(expected, row);
    }

    @Test
    public void should_set_new_column_name() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");
        row.getRowMetadata().getById("0000").setName("source");
        row.getRowMetadata().getById("0001").setName("selected");

        parameters.remove(NumericOperations.OPERAND_PARAMETER);

        // when
        action.applyOnColumn(row, new ActionContext(new TransformationContext(), row.getRowMetadata()), parameters, "0000");

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source + selected").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_set_new_column_name_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");
        row.getRowMetadata().getById("0000").setName("source");
        row.getRowMetadata().getById("0001").setName("selected");

        parameters.remove(NumericOperations.SELECTED_COLUMN_PARAMETER);
        parameters.put(NumericOperations.MODE_PARAMETER, NumericOperations.CONSTANT_MODE);

        // when
        action.applyOnColumn(row, new ActionContext(new TransformationContext(), row.getRowMetadata()), parameters, "0000");

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source + 2").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test(expected = TDPException.class)
    public void should_not_apply_on_wrong_column() {
        // given
        DataSetRow row = getRow("5");

        // when
        action.applyOnColumn(row, new ActionContext(new TransformationContext(), row.getRowMetadata()), parameters, "0000");
    }

    @Test(expected = TDPException.class)
    public void should_fail_wrong_column() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");
        row.getRowMetadata().getById("0000").setName("source");
        row.getRowMetadata().getById("0001").setName("selected");

        parameters.put(NumericOperations.SELECTED_COLUMN_PARAMETER, "youpi");

        // when
        action.applyOnColumn(row, new ActionContext(new TransformationContext(), row.getRowMetadata()), parameters, "0000");
    }

    @Test(expected = TDPException.class)
    public void should_fail_constant_missing_operand() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.remove(NumericOperations.OPERAND_PARAMETER);
        parameters.put(NumericOperations.MODE_PARAMETER, NumericOperations.CONSTANT_MODE);

        // when
        action.applyOnColumn(row, new ActionContext(new TransformationContext(), row.getRowMetadata()), parameters, "0000");
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.INTEGER)));
        assertTrue(action.acceptColumn(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptColumn(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

}