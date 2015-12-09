package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.jayway.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.json.JsonErrorCode;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetGovernance;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.error.DataSetErrorCodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


/**
 * Unit test for Export API.
 */
public class DataSetAPITest extends ApiServiceTestBase {


    @Test
    public void testDataSetUpdate() throws Exception {
        // given a created dataset
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");

        // when it's updated
        given().body(IOUtils.toString(PreparationAPITest.class.getResourceAsStream("t-shirt_100.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/api/datasets/" + dataSetId + "?name=testDataset").asString();

        // then, the content is updated
        String dataSetContent = when().get("/api/datasets/" + dataSetId + "?metadata=false&columns=true").asString();
        final String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("t-shirt_100.csv.expected.json"));
        assertThat(dataSetContent, sameJSONAs(expectedContent).allowingExtraUnexpectedFields());
    }

    @Test
    public void testDataSetList() throws Exception {
        // given
        final String dataSetId = createDataset( "dataset/dataset.csv", "testDataset", "text/csv" );

        // when
        final String list = when().get("/api/datasets").asString();

        // then
        assertTrue( list.contains( dataSetId ) );
    }

    @Test
    public void testDataSetListWithDateOrder() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        // given
        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");

        // when (sort by date, order is desc)
        String list = when().get("/api/datasets?sort={sort}&order={order}", "date", "desc").asString();

        // then
        Iterator<JsonNode> elements = mapper.readTree(list).elements();
        String[] expectedNames = new String[] {dataSetId2, dataSetId1};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("id").asText(), is(expectedNames[i++]));
        }

        // when (sort by date, order is desc)
        list = when().get("/api/datasets?sort={sort}&order={order}", "date", "asc").asString();

        // then
        elements = mapper.readTree(list).elements();
        expectedNames = new String[] {dataSetId1, dataSetId2};
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("id").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void testDataSetListWithNameOrder() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        // given
        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");

        // when (sort by date, order is desc)
        String list = when().get("/api/datasets?sort={sort}&order={order}", "name", "desc").asString();

        // then
        Iterator<JsonNode> elements = mapper.readTree(list).elements();
        String[] expectedNames = new String[] {dataSetId2, dataSetId1};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("id").asText(), is(expectedNames[i++]));
        }

        // when (sort by date, order is desc)
        list = when().get("/api/datasets?sort={sort}&order={order}", "date", "asc").asString();

        // then
        elements = mapper.readTree(list).elements();
        expectedNames = new String[] {dataSetId1, dataSetId2};
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("id").asText(), is(expectedNames[i++]));
        }
    }


    /**
     * Simple dataset deletion case.
     */
    @Test
    public void testDataSetDelete() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");

        final String list = when().get("/api/datasets").asString();
        assertTrue(list.contains(dataSetId));

        // when
        when().delete("/api/datasets/" + dataSetId).asString();
        final String updatedList = when().get("/api/datasets").asString();

        // then
        assertEquals("[]", updatedList);
    }

    /**
     * DataSet deletion test case when the dataset is used by a preparation.
     */
    @Test
    public void testDataSetDeleteWhenUsedByPreparation() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");
        createPreparationFromDataset(dataSetId, "testPreparation");

        // when/then
        final Response response = when().delete( "/api/datasets/" + dataSetId );

        //then
        final int statusCode = response.statusCode();
        assertThat( statusCode, is( 409 ) );

        final String responseAsString = response.asString();
        final JsonPath json = from(responseAsString);
        assertThat(json.get("code"), is("TDP_API_DATASET_STILL_IN_USE"));
    }

    @Test
    public void testDataSetCreate() throws Exception {
        // given
        final String dataSetId = createDataset( "dataset/dataset.csv", "tagada", "text/csv" );
        final InputStream expected = PreparationAPITest.class.getResourceAsStream(
            "dataset/expected_dataset_with_metadata.json" );

        // when
        final String contentAsString = when().get("/api/datasets/{id}?metadata=true&columns=false", dataSetId).asString();

        // then
        assertThat(contentAsString, sameJSONAsFile( expected ));
    }

    @Test
    public void testDataSetCreate_clone() throws Exception {

        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "tagada", "text/csv");
        InputStream expected = PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_metadata.json");

        // when
        String contentAsString = when().get("/api/datasets/{id}?metadata=true&columns=false", dataSetId).asString();

        // then
        assertThat( contentAsString, sameJSONAsFile( expected ) );

        String cloneName = "foo bar";

        final String clonedDataSetId = given() //
            .queryParam( "cloneName", cloneName) //
            .when() //
            .put("/api/datasets/clone/{id}", dataSetId) //
            .asString();

        Assertions.assertThat( clonedDataSetId ).isNotEmpty().isNotEqualTo( dataSetId );

        Response response = when().get("/api/datasets/{id}?metadata=true&columns=false", clonedDataSetId);

        contentAsString = response.asString();

        ObjectMapper objectMapper = new ObjectMapper();

        DataSet dataSet = objectMapper.readValue( contentAsString, DataSet.class );

        DataSetMetadata dataSetMetadata = dataSet.getMetadata();

        Assertions.assertThat( dataSetMetadata.getName() ).isEqualTo( cloneName );

        Assert.assertEquals( HttpStatus.OK.value(), response.getStatusCode() );

        expected = PreparationAPITest.class.getResourceAsStream( "dataset/expected_dataset_with_metadata_clone.json" );

        // then
        assertThat( contentAsString, sameJSONAsFile( expected ) );

    }

    @Test
    public void testDataSetCreate_clone_already_exists() throws Exception {

        // create beer folder
        Response response = RestAssured.given() //
            .queryParam("path", "beer").when() //
            .put("/api/folders");

        Assert.assertEquals( HttpStatus.OK.value(), response.getStatusCode() );

        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "tagada", "text/csv", "beer");
        InputStream expected = PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_metadata.json");

        // when
        String contentAsString = when().get("/api/datasets/{id}?metadata=true&columns=false", dataSetId).asString();

        // then
        assertThat( contentAsString, sameJSONAsFile( expected ) );

        String cloneName = "foo bar";

        String clonedDataSetId = given() //
            .queryParam("folderPath", "foo") //
            .queryParam( "cloneName", cloneName) //
            .when() //
            .put("/api/datasets/clone/{id}", dataSetId) //
            .asString();

        Assert.assertEquals( HttpStatus.OK.value(), response.getStatusCode() );

        Assertions.assertThat( clonedDataSetId ).isNotEmpty().isNotEqualTo( dataSetId );

        response = when().get("/api/datasets/{id}?metadata=true&columns=false", clonedDataSetId);

        contentAsString = response.asString();

        ObjectMapper objectMapper = new ObjectMapper();

        DataSet dataSet = objectMapper.readValue( contentAsString, DataSet.class );

        DataSetMetadata dataSetMetadata = dataSet.getMetadata();

        Assertions.assertThat( dataSetMetadata.getName() ).isEqualTo( cloneName );

        expected = PreparationAPITest.class.getResourceAsStream( "dataset/expected_dataset_with_metadata_clone.json" );

        // then
        assertThat( contentAsString, sameJSONAsFile( expected ) );

        response = given() //
            .queryParam("folderPath", "foo") //
            .queryParam( "cloneName", cloneName) //
            .when() //
            .put("/api/datasets/clone/{id}", dataSetId);

        Assert.assertEquals( HttpStatus.BAD_REQUEST.value(), response.getStatusCode() );

    }

    @Test
    public void testDataSetGetWithSample() throws Exception {
        // given
        final String dataSetId = createDataset("t-shirt_100.csv", "test_sample", "text/csv");

        // when
        final String contentAsString = when().get("/api/datasets/{id}?metadata=false&columns=false&sample=16", dataSetId)
                .asString();

        // then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(contentAsString);
        JsonNode records = rootNode.findPath("records");
        assertThat(records.size(), is(16));
    }

    @Test
    public void testDataSetGetWithSampleZeroOrFull() throws Exception {
        // given
        final String dataSetId = createDataset("t-shirt_100.csv", "test_sample", "text/csv");

        // when 0
        String contentAsString = when().get("/api/datasets/{id}?metadata=false&columns=false&sample=0", dataSetId)
                .asString();

        // then full content
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(contentAsString);
        JsonNode records = rootNode.findPath("records");
        assertThat(records.size(), is(100));

        // when full
        contentAsString = when().get("/api/datasets/{id}?metadata=false&columns=false&sample=full", dataSetId).asString();

        // then full content
        rootNode = mapper.readTree(contentAsString);
        records = rootNode.findPath("records");
        assertThat(records.size(), is(100));
    }

    @Test
    public void testDataSetGetWithSampleWhenSampleIsInvalid() throws Exception {
        // given
        final String dataSetId = createDataset("t-shirt_100.csv", "test_sample", "text/csv");

        // when (decimal number)
        String contentAsString = when().get("/api/datasets/{id}?metadata=false&columns=false&sample=10.6", dataSetId).asString();

        // then (full content)
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(contentAsString);
        JsonNode records = rootNode.findPath("records");
        assertThat(records.size(), is(100));

        // when (
        contentAsString = when().get("/api/datasets/{id}?metadata=false&columns=false&sample=ghqmskjh", dataSetId).asString();

        // then (full content)
        rootNode = mapper.readTree(contentAsString);
        records = rootNode.findPath("records");
        assertThat(records.size(), is(100));

    }

    @Test
    public void testDataSetCreateWithSpace() throws Exception {
        // given
        String dataSetId = createDataset("dataset/dataset.csv", "Test with spaces", "text/csv");

        // when
        final DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);

        // then
        assertNotNull(metadata);
        assertEquals("Test with spaces", metadata.getName());
    }

    @Test
    public void testDataSetColumnSuggestions() throws Exception {
        // given
        final String columnDescription = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("suggestions/firstname_column_metadata.json"));

        // when
        final String content = given().body(columnDescription).when().post("/api/transform/suggest/column").asString();

        // then
        assertThat(content, sameJSONAs("[]")); // All values in column are valid, no corrective action proposed.
    }

    @Test
    public void testDataSetColumnActions() throws Exception {
        // given
        final String columnDescription = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("suggestions/firstname_column_metadata.json"));

        // when
        final String content = given().body(columnDescription).when().post("/api/transform/actions/column").asString();

        // then
        final InputStream expected = PreparationAPITest.class.getResourceAsStream("suggestions/firstname_column_actions.json");
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testDataSetLineActions() throws Exception {
        // when
        final String content = given().when().get("/api/transform/actions/line").asString();

        // then
        final InputStream expected = PreparationAPITest.class.getResourceAsStream("suggestions/all_line_scope_actions.json");
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testDataSetActions() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");

        // when
        final String contentAsString = when().get("/api/datasets/{id}/actions", dataSetId).asString();

        // then
        assertThat(contentAsString, sameJSONAs("[]"));
    }

    @Test
    public void testLookupActionsActions() throws Exception {
        // given
        final String firstDataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");
        final String dataSetId = createDataset("dataset/dataset_cars.csv", "cars", "text/csv");
        final String thirdDataSetId = createDataset("dataset/dataset.csv", "third", "text/csv");

        List<String> expectedIds = Arrays.asList(firstDataSetId, thirdDataSetId);

        // when
        final String actions = when().get("/api/datasets/{id}/actions", dataSetId).asString();

        // then
        final JsonNode jsonNode = builder.build().readTree(actions);
        // response is an array
        assertThat(jsonNode.isArray(), is(true));
        // an array of 2 entries
        ArrayNode lookups = (ArrayNode) jsonNode;
        assertThat(lookups.size(), is(2));

        // let's check the url of the possible lookups
        for (int i = 0; i < lookups.size(); i++) {
            final JsonNode lookup = lookups.get(i);
            final ArrayNode parameters = (ArrayNode) lookup.get("parameters");
            for (int j = 0; j < parameters.size(); j++) {
                final JsonNode parameter = parameters.get(j);
                if (StringUtils.equals(parameter.get("name").asText(), "url")) {
                    final String url = parameter.get("default").asText();
                    // the url id must be known
                    assertThat(expectedIds.stream().filter(s -> url.contains(s)).count(), is(1L));
                }
            }
        }
    }

    @Test
    public void testAskCertification() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "tagada", "text/csv");

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        assertNotNull(dataSetMetadata.getGovernance());
        assertEquals(DataSetGovernance.Certification.NONE, dataSetMetadata.getGovernance().getCertificationStep());

        // when
        when().put("/api/datasets/{id}/processcertification", dataSetId).then().statusCode(HttpStatus.OK.value());

        // then
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        assertNotNull(dataSetMetadata.getGovernance());
        assertEquals(DataSetGovernance.Certification.PENDING, dataSetMetadata.getGovernance().getCertificationStep());
        assertThat(dataSetMetadata.getRow().getColumns(), not(empty()));

        // when
        when().put("/api/datasets/{id}/processcertification", dataSetId).then().statusCode(HttpStatus.OK.value());

        // then
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        assertNotNull(dataSetMetadata.getGovernance());
        assertEquals(DataSetGovernance.Certification.CERTIFIED, dataSetMetadata.getGovernance().getCertificationStep());
        assertThat(dataSetMetadata.getRow().getColumns(), not(empty()));
    }

    @Test
    public void testDataSetCreateUnsupportedFormat() throws Exception {
        // given
        final String datasetContent = IOUtils.toString(DataSetAPITest.class.getResourceAsStream("dataset/dataset.ods"));
        final int metadataCount = dataSetMetadataRepository.size();
        // then
        final Response response = given().body(datasetContent).when().post("/api/datasets");
        assertThat(response.getStatusCode(), is(400));
        JsonErrorCode code = builder.build().reader(JsonErrorCode.class).readValue(response.getBody().print());
        assertThat(code.getCode(), is(DataSetErrorCodes.UNSUPPORTED_CONTENT.getCode()));
        assertThat(dataSetMetadataRepository.size(), is(metadataCount)); // No data set metadata should be created
    }

}
