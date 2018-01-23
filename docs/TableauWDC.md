# Tableau Web Data Connector (WDC)

This document describes how to use the Tableau Web Data Connector (WDC) to access data
from the Cerebro Access Platform.

NOTE: the Cerebro-Tableau Web Data Connector (WDC) is a "beta" product. Depending on
usability and requirement changes, the interface may change.

The purpose of the Tableau Web Data Connector (WDC) is to provide access to data from the
Cerebro Access Platform from within the Tableau product suite. A Tableau license is
required to use the Tableau Web Data Connector. This document does not provide any
instructions on how to use Tableau other than the steps required to start the Web Data
Connector and select a representative data set.

The Tableau Web Data Connector was written and tested using Tableau Desktop (tm)
Professional Edition version 10.1.4, 64-bit an Apple Inc. Computer (tm) running
macOS Sierra Version 10.12.3.

## Using the Tableau Web Data Connector (WDC)

These instructions assume that you have access to the Tableau Desktop (tm)
Professional Edition product.

1. Open Tableau Desktop.  The following screen should appear:

![Tableau Opening Screen](https://s3.amazonaws.com/cerebrodata-docs/images/TableauOpeningScreen.png)

2. Select Web Data Connector from the left-hand navigation menu

3. The following screen should appear:

![Tableau Selection Page](https://s3.amazonaws.com/cerebrodata-docs/images/TableauConnectorSelection.png)

NOTE: the list of 'Recent Connectors' is illustrative and may not match your list.

4. Click in the box labeled 'Enter your web data connector URL here'. Enter the URL of
the _cdas_rest_api_ in the box followed by **/wdc**. The following page will appear:

![Tableau Web Data Connectior](https://s3.amazonaws.com/cerebrodata-docs/images/TableauWebDataConnector.png)

5. Enter a valid authentication token in the text box above the 'Accept Auth Token'
button and press the 'Accept Auth Token' button. A valid authentication token can be
acquired by referring to the 'Getting a token' section of [authentication document](Authentication.md).

6. (Optional) Modify the "Max records" field. This field will dictate how many records
per dataset will be fetched at most. Lower numbers can significantly improve performance.

7. Click the 'Get Data!' link when it becomes highlighted.

8. A page similar to the following page should appear. Please note that the data sets in
this list are illustrative only.

![Tableau Web Data Connector Data Sets](https://s3.amazonaws.com/cerebrodata-docs/images/TableauDataSets.png)

9. Select a data set from the left hand list. The schema of the selected data set should
appear. The page should be similar to the following:

![Tableau Web Data Connector Schema](https://s3.amazonaws.com/cerebrodata-docs/images/TableauSchema.png)

10. Click the 'Update Now' link to load the data. A page similar to the following should
appear. The length of time for this step to complete will vary depending on the amount of
data in your selected data set.

![Tableau Data View](https://s3.amazonaws.com/cerebrodata-docs/images/TableauDataView.png)

## Error Handling

In the case where only part of the catalog could be loaded due to errors, the partial
catalog will be populated. If you believe this is occurring:

1. Verify that the catalog is accessible to you from another client (e.g. hive or hue)
2. Consult the CDAS REST server log for details.

The catalog entries could fail to load due to any number of issues, for example:

1. Corrupt metadata such as an invalid view definition.
2. Intermittent network or system outage.

## Error Reporting

When errors are detected, because of either user error, system error or communication
error the following dialog will be presented:

   ![Tableau Exception Report](https://s3.amazonaws.com/cerebrodata-docs/images/TableauExceptionReport.png)

Clicking on 'Show Details' presents additional information, such as:

   ![Tableau Exception Detail](https://s3.amazonaws.com/cerebrodata-docs/images/TableauExceptionDetail.png)

The contents of these pages are illustrative only. The actual contents may vary,
depending on the reason for the error or exception.

## Common Error Messages

The following are a list of known exceptions and the root cause for each exception:

1. *User does not have access to any tables.*

    The catalog returned an empty list of table names. Verify that the token you entered
    is valid and that if it is valid, that there are tables assigned in the catalog for
    the user.

2. *Error reading data for table: **table name**.*

    An internal error or communications error occurred while retrieving data for the
    table _table name_. If it is a communication error, retry the request. If it is an
    internal error, report the problem along with the scenario you attempted.

3. *Error loading tables in database.*

    An internal error or communications error occurred while loading catalog.
