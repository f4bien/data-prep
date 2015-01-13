#![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend") Data Preparation Project

| _Project_                  | _Description_ |
|:-------------------------|-------------|
| [dataprep-common](dataprep-common)          | *everything (utilities) common to all projects but not related to dataprep business* |
| [dataprep-dataset](dataprep-dataset)         | *back-end service for data set operations (create, update, content index, get data set metadata...)* |
| [dataprep-schema-analysis](dataprep-schema-analysis) | *stuff used to analyse (format, separator, schema, to-json) input data* |
| [dataprep-webapp](dataprep-webapp)          | *web app client and dev server (real server will in another project)* |
| [json_evaluation](json_evaluation)          | *evaluation of different json lib performance (jackson is identified as the faster). To remove?* |
| [recipe_processor](recipe_processor)         | *currently different tests of the Spark engine + other technos tests (SparkSql)* |
| [sample_builder](sample_builder)           | *Talend job builder (based on jobscript) [file/stream] -> [sampling/json file]* |

## build
- All project are maven based.
- A parent in pom build the web-app and its dependencies.
- See [dataprep-dataset](/dataprep-dataset/) for specific build instructions
