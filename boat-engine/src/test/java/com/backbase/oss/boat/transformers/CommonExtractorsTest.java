package com.backbase.oss.boat.transformers;

import com.backbase.oss.boat.example.NamedExample;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CommonExtractorsTest {

    @Test
    public void test_headerExamples_noExample() {
        Header header = new Header();
        List<NamedExample> examples = CommonExtractors.headerExamples("some-name", header);
        assertTrue(examples.isEmpty());
    }

    @Test
    public void test_headerExamples_nullExamplesMap() {
        Header header = new Header().examples(null);
        List<NamedExample> examples = CommonExtractors.headerExamples("some-name", header);
        assertTrue(examples.isEmpty());
    }

    @Test
    public void test_headerExamples_examplesMapNotEmpty() {
        Schema<?> schema = new Schema<>().name("MySchema");
        String key = "example-1";
        Example example = new Example().value("1234");
        Header header = new Header()
                .addExample(key, example)
                .schema(schema);
        List<NamedExample> examples = CommonExtractors.headerExamples("Dummy-name", header);
        assertFalse(examples.isEmpty());
        assertEquals(1, examples.size());
        assertEquals(example, examples.get(0).getExample());
        assertEquals(schema.getName() + "-" + key, examples.get(0).getName());
    }

    @Test
    public void test_headerExamples_ownExamplePresent() {
        String name = "X-Items-Count";
        Header header = new Header().example("1234");
        List<NamedExample> examples = CommonExtractors.headerExamples(name, header);
        assertFalse(examples.isEmpty());
        assertEquals(1, examples.size());
        assertEquals(name, examples.get(0).getName());
    }

    @Test
    public void test_headerExamples_ownExamplePresentAndExamplesMapNotEmpty() {
        Schema<?> schema = new Schema<>().$ref("#/components/schemas/MySchema");
        String name = "X-Items-Count";
        String key = "example-1";
        Example example = new Example().value("dummy-val");
        Header header = new Header()
                .example("1234")
                .schema(schema)
                .addExample(key, example);
        List<NamedExample> examples = CommonExtractors.headerExamples(name, header);
        assertFalse(examples.isEmpty());
        assertEquals(2, examples.size());
        assertTrue(examples.stream().anyMatch(namedExample ->
                namedExample.getExample() == example && namedExample.getName().equals("MySchema-" + key))
        );
        assertTrue(examples.stream().anyMatch(namedExample ->
                namedExample.getExample().getValue() == header.getExample() && namedExample.getName().equals(name))
        );
    }

    @Test
    public void test_parameterExamples_nullExamples() {
        Parameter parameter = new Parameter();
        List<NamedExample> examples = CommonExtractors.parameterExamples(parameter);
        assertTrue(examples.isEmpty());
    }

    @Test
    public void test_parameterExamples_examplesMapNotEmpty() {
        Example example = new Example().value("some-value");
        Parameter parameter = new Parameter()
                .name("accountId")
                .addExample("example", example);
        List<NamedExample> examples = CommonExtractors.parameterExamples(parameter);
        assertEquals(1, examples.size());
        assertEquals(example, examples.get(0).getExample());
        assertEquals(parameter.getName(), examples.get(0).getName());
    }

    @Test
    public void test_parameterExamples_ownExamplePresent() {
        Parameter parameter = new Parameter()
                .name("accountId")
                .example("a8346");
        List<NamedExample> examples = CommonExtractors.parameterExamples(parameter);
        assertEquals(1, examples.size());
        assertEquals(parameter.getExample(), examples.get(0).getExample().getValue());
        assertEquals("param-" + parameter.getName(), examples.get(0).getName());
    }

    @Test
    public void test_parameterExamples_ownExamplePresentAndExamplesMapNotEmpty() {
        Example example = new Example().value("some-value");
        Parameter parameter = new Parameter()
                .name("accountId")
                .example("a1234-b5")
                .addExample("example", example);
        List<NamedExample> examples = CommonExtractors.parameterExamples(parameter);
        assertEquals(2, examples.size());
        assertTrue(examples.stream().anyMatch(namedExample ->
                namedExample.getExample() == example && namedExample.getName().equals(parameter.getName())
        ));
        assertTrue(examples.stream().anyMatch(namedExample ->
                namedExample.getExample().getValue() == parameter.getExample()
                        && namedExample.getName().equals("param-" + parameter.getName())
        ));
    }

    @Test
    public void test_parameterExamples_ownExamplePresentAndExamplesMapNotEmptyAndContentExamples() {
        Example example = new Example().value("some-value");
        String mediaTypeExampleName = "MediaTypeExampleName1";
        String mediaType$ref = "../openapi.yaml#/components/schemas/SomeName";
        Example mediaTypeExample = new Example().value("some-data");
        Parameter parameter = new Parameter()
                .name("accountId")
                .example("a1234-b5")
                .addExample("example", example).content(new Content()
                        .addMediaType("text/plain", new MediaType()
                                .schema(new Schema<>().$ref(mediaType$ref))
                                .addExamples(mediaTypeExampleName, mediaTypeExample)
                        ));
        List<NamedExample> examples = CommonExtractors.parameterExamples(parameter);
        assertEquals(3, examples.size());
        assertTrue(examples.stream().anyMatch(namedExample ->
                namedExample.getExample() == example && namedExample.getName().equals(parameter.getName())
        ));
        assertTrue(examples.stream().anyMatch(namedExample ->
                namedExample.getExample().getValue() == parameter.getExample()
                        && namedExample.getName().equals("param-" + parameter.getName())
        ));
        assertTrue(examples.stream().anyMatch(namedExample ->
                namedExample.getExample() == mediaTypeExample
                        && namedExample.getName().equals("SomeName-" + mediaTypeExampleName)
        ));
    }

    @Test
    public void test_contentExamples_noMediaType() {
        Content content = new Content();
        List<NamedExample> examples = CommonExtractors.contentExamples(content);
        assertTrue(examples.isEmpty());
    }

    @Test
    public void test_contentExamples_mediaTypeAndItsEncodingPresent() {
        String encodingExampleName = "jim";
        Example encodingExample = new Example().value("blacksmith");
        String headerSchemaName = "MySchema";
        Map<String, Header> encodingHeadersExamples = new HashMap<>();
        encodingHeadersExamples.put("X-Some-Header", new Header()
                .addExample(encodingExampleName, encodingExample)
                .schema(new Schema<>().name(headerSchemaName)));
        Encoding encoding = new Encoding().headers(encodingHeadersExamples);
        String mediaTypeExampleName = "john";
        Example mediaTypeExample = new Example().value("carpenter");
        String mediaTypeSchemaName = "MyMediaType";
        MediaType mediaType = new MediaType()
                .schema(new Schema<>().name(mediaTypeSchemaName))
                .addExamples(mediaTypeExampleName, mediaTypeExample)
                .addEncoding("my-encoding", encoding);
        Content content = new Content()
                .addMediaType("text/plain", mediaType);
        List<NamedExample> examples = CommonExtractors.contentExamples(content);
        assertEquals(2, examples.size());
        assertTrue(examples.stream().anyMatch(
                namedExample -> namedExample.getExample() == encodingExample
                        && namedExample.getName().equals(headerSchemaName + "-" + encodingExampleName))
        );
        assertTrue(examples.stream().anyMatch(
                namedExample -> namedExample.getExample() == mediaTypeExample
                        && namedExample.getName().equals(mediaTypeSchemaName + "-" + mediaTypeExampleName))
        );
    }
}