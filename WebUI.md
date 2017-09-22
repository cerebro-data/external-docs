# Cerebro Web UI

The Cerebro Web UI provides a user interface to browse the Cerebro datasets available to you, in addition to your account information.

## Get started

To access the Web UI, first set up a Cerebro cluster. Once the cluster is set up,
determine the endpoints that have been created, for example by running
`cerebro_cli clusters endpoints <cluster_id>`. Find the host and port of the component
named `cerebro_web:webui` and access `http://<host>:<port>`
(or `https://<host>:<port>` if using SSL) in a web browser.

You will see a login screen. You can input your Cerebro access token to log in.
If you've set up SSO or LDAP, you will have the option to input your username/password
to log into Cerebro (after which point you will be able to view your access token).

## Features

In general, the Web UI provides read-only browsing of your Cerebro environment.

### Home Page

After you login, you will land on the Home Page. From this page, you can see

- Account information, including
  - Your Cerebro username
  - The groups you belong to
  - The roles you have been granted, and the groups granting you those roles
  - Your access token, including when it will expire

### Dataset Page

From the navigation tabs in the upper part of the page, you can access the
Datasets Page. From here, all datasets available to you in the system will show
up in the left side bar, grouped by the database they belong to. To get details
for a specific dataset, expand the database section by clicking on it; then, click
on the dataset you'd like to see details for. The details will load in the
main content section of the page.

The main features on this page include

- Dataset browsing in the left side bar
  - You can see all datasets in the system for which you have access to at least one
 column
  - Datasets are grouped by databases, which can be expanded by clicking on the database
 name
  - Dataset search, enabled by typing into the search field in the upper part of the
 left side bar
    - Any dataset whose name contains your input as a substring will be shown
    - A search is performed after every keystroke, for quick searching
    - To clear the search, hit the Escape key
- Dataset metadata
  - Click on a dataset in the dataset browser to view its metadata
  - The metadata header contains information, including
    - Database name
    - Dataset name
    - Owner
    - Description
  - The dataset schema is displayed in full
    - Each column in the dataset is shown, including
      - Name
      - Type
      - Access (whether you have read-access to view the cells in this column)
    - If a column's name has a gray background, it is a partitioning column
    - If you do not have access to a column, click "See Groups" in the row
      - You will see the list of groups to which your account will need to be added
   in order to get access to this column
  - Dataset preview
    - After clicking on a dataset from the dataset browser, click on "Show Preview"
  in the upper right part of the page
      - A sample of the rows in that dataset will show in a dialog
    - No more than 200 rows will be shown (to find out how to view more,
   see "Dataset usage" below)
    - You will not see information for columns for which you have no access
    - The dialog can scroll vertically *as well as* horizontally
 - Dataset usage
   - After clicking on a dataset from the dataset browser, click "Get Started"
  in the upper right part of the page
   - Up to four sections of sample code will be show (Spark, Hive, Python, and R)
   - Click on the tab of your choice and copy/paste the code into the
  associated environment to use this dataset outside of the Web UI

### Credentials

Your credentials are saved in the browser until either your token expires, or you
explicitly log out. Be aware of this when sharing access to your computer.

To logout, click your username in the upper right part of the screen, then click "Logout".

### Troubleshooting

If you experience a problem with a particular aspect of the Web UI, or notice that
features do not work as described or expected, please take a screenshot or video capture.
If possible, click the "About" link in the upper right part of the page and take note
(e.g. another screenshot) of the details in the dialog that pops up. These version
numbers will help to diagnose and fix issues that arise.
