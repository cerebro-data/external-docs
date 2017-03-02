# Tableau Web Data Connector (WDC)
This document describes how to use the Tableau Web Data Connector (WDC) to access data from the
Cerebro Access Platform.

NOTE: the Cerebro-Tableau Web Data Connector (WDC) is a "beta" product.  Depending on usability and
requirement changes, the interface may change.

The purpose of the Tableau Web Data Connector (WDC) is to provide access to data from the
Cerebro Access Platform  from within the Tableau product suite.  A Tableau license is required to
use the Tableau Web Data Connector.  This document does not provide any instructions on how
to use Tableau other than the steps required to start the Web Data Connector and select
a representative data set.

The Tableau Web Data Connector was written and tested using Tableau Desktop (tm) Professional
Editon version 10.1.4, 64-bit an Apple Inc. Computer (tm) running macOS Sierra Version 10.12.3.

## Using the Tableau Web Data Connector (WDC)

These instructions assume that you have access to the Tableau Desktop (tm) Professional Edition
product.  

   1. Open Tableau Desktop.  The following screen should appear:

   ![Tableau Opening Screen](https://s3.amazonaws.com/cerebro-data-docs/images/TableauOpeningScreen.png)

   2. Select Web Data Connector from the left-hand navigation menu

   3. The following screen should appear:

   ![Tableau Selection Page](https://s3.amazonaws.com/cerebro-data-docs/images/TableauConnectorSelection.png)

   NOTE: the list of 'Recent Connectors' is illustrative and may not match your list.

   4. Click in the box labeled 'Enter your web data connector URL here'.  Enter the URL of the
_cdas_rest_api_ in the box followed by **/wdc**.  The following page will appear:

   ![Tableau Web Data Connectior](https://s3.amazonaws.com/cerebro-data-docs/images/TableauWebDataConnector.png)

   5. Enter a valid authentication token in the text box above the 'Accept Auth Token' button and
press the 'Accept Auth Token' button.  A valid authentication token can be acquired by _*insert link to instructions*_.

   6. Click the 'Get Data!' link when it becomes highlighted.

   7. A page similar to the following page should appear.  Please note that the data sets in this
list are illustrative only.

     ![Tableau Web Data Connector Data Sets](https://s3.amazonaws.com/cerebro-data-docs/images/TableauDataSets.png)

   8. Select a data set from the left hand list.  The schema of the selected data set should
appear.  The page should be similar to the following:

    ![Tableau Web Data Connector Schema](https://s3.amazonaws.com/cerebro-data-docs/images/TableauSchema.png)

   9. Click the 'Update Now' link to load the data.  A page similar to the following should
appear.  The length of time for this step to complete will vary depending on the amount of data in
your selected data set.

    ![Tableau Data View](https://s3.amazonaws.com/cerebro-data-docs/images/TableauDataView.png)

    ## Error Reporting

     When errors are detected, because of either user error, system error or communication error
the following dialog will be presented:  

    ![Tableau Exception Report](https://s3.amazonaws.com/cerebro-data-docs/images/TableauExceptionReport.png)

    Clicking on 'Show Details' presents additional information, such as:  

    ![Tableau Exception Detail](https://s3.amazonaws.com/cerebro-data-docs/images/TableauExceptionDetail.png)

The contents of these pages are illustrative only.  The actual contents may vary, depending on the
reason for the error or exception.

## Common Error Messages

The following are a list of known exceptions and the root cause for each exception:

   1. *There are no tables assigned to this user.*

   The catalog returned an empty list of table names.  Verify that the token you entered
   is valid and that if it is valid, that there are tables assigned in the catalog for
   the user.

   2. _Error reading data for table: **table name**.  Error is: **error text**._

   An internal error or communications error occurred while retrieving data for
   the table _table name_.  If it is a communication error, retry the request.  If
   it is an internal error, report the problem along with the scenario you attempted.

   3. _Error reading schema.  Error is: **error text**._

   An internal error or communications error occurred while retrieving schema information.
   If it is a communication error, retry the request.  If it is an internal error, report
   the problem along with the scenario you attempted.


