# Cerebro Web UI

The Cerebro Web UI provides a user interface to: view your account information, browse the 
datasets available to you in Cerebro, and inspect group permissions on any dataset for which 
you have all privileges.

## Get started

To access the Web UI, first set up a Cerebro cluster. Once the cluster is set up,
determine the endpoints that have been created, for example by running
`cerebro_cli clusters endpoints <cluster_id>`. Find the host and port of the component
named `cerebro_web:webui` and access `http://<host>:<port>` (or `https://<host>:<port>`
if using SSL) in a web browser.

You will see a login screen that can be configured to provide various login options 
depending on your cluster configuration, including:
- **Cerebro token login**: this option is always available for log in as long as the user
can get a Cerebro token. 
- **username/password login**: if the cluster is configured to authenticate with LDAP, 
username and password login is available
- **OAuth login**: OAuth login is available if the cluster is configured for it 
and there is an auth server set up to handle it.

Enter the necessary credentials to log in to the WebUI. 

## Features

In general, the Web UI provides read-only browsing of your Cerebro environment, including:
search and filtering of available datasets, dataset metadata views (including schema info), 
information about access to datasets and fields, dataset previews, basic usage information
for getting connecting to datasets from analytics environments, and information about the
logged-in user's account and credentials.

### Home Page

After you login, you will land on the Home Page. From this page, you can see

- Account information, including
  - Your Cerebro username
  - The groups you belong to
  - The roles you have been granted, and the groups granting you those roles
  - Your access token, including when it will expire

### Datasets Page

From the navigation tabs in the upper part of the page, you can access the Datasets page.
The Datasets page allows you to browse, search for, filter, and inspect the datasets 
available to you in the Cerebro deployment. For users who have all access to some number of
datasets, the Datasets page also enables checking which groups have access to those 
datasets and which fields users in those groups can read.

#### Browsing Datasets
By Default the Datasets page will show a paged list of datasets available to you, including
summary details. Any dataset where you have any level of access will appear in this list. 
Note that this means there could be datasets in the deployment that are not shown if you
have no read access to any field in them. 

Each dataset in the list shows some basic summary information such as what database
it is in, and who the owner is. Clicking on a dataset will bring you to the dataset
details page (described below).

The left side bar contains search and filter options for the dataset list, including:

- **A search box** where you can search by dataset name or the name of the database it is 
part of. Any dataset whose name contains your input as a substring will be shown. You can 
clear this box with the escape key.
- **'Include in search' checkboxes** to specify whether you want to search just dataset 
name, database name, or both.
- **A 'Filter by database' multi-select box** that allows you to filter the list to only 
datasets in a particular database or set of databases. 
- **A 'Search' button** that will apply the search and update the dataset list to match
your currently selected filters and search.

Additionally, if there are databases to which you have all access, you will see a 
**Compare Access** button in the top right of the datasets list. Clicking on this button
will allow you to inspect which groups have access to those databases. When you click 
**Compare Access**:
- The list of datasets displayed will be filtered to only show those databases for which 
you have all access
- You will see a multi-select box where you can select a group or set of groups to see 
which datasets those groups have access to. Only groups with some level of access to your 
datasets will be shown in this multi-select; any group not in the available list has no 
access to any of the datasets that are visible.
- An **Apply** button that applies the currently selected set of groups to the access 
column view.
- A column is added to every dataset showing whether the group or groups being inspected 
can access any field in the dataset. A **checkmark** in this column indicates that the 
group or set of groups combined can access the dataset, a **lock** icon is displayed when
the groups have no access.
  
  *Note that access is treated as a logical OR of the groups' individual access--if any 
  of the selected groups have access to a dataset, you will see a checkmark for that 
  group.*

#### Dataset Details
Clicking on a dataset in the dataset list will open that dataset's details page. This page 
contains:
  - The dataset's metadata, including
    - Database name
    - Dataset name
    - Owner
    - Description
  - The dataset schema described in full
    - Each column in the dataset is shown, including
      - Name
      - Type
      - Access (whether you have read-access to view the cells in this column)
    - If a column's name has a gray background, it is a partitioning column
    - If you do not have access to a column, click "See Groups" in the row
      - You will see a list of groups; your account will need to be added to at least
      one of these groups to gain access to the column
  - Dataset preview
    - After clicking on a dataset from the dataset browser, click on "Show Preview" in
      the upper right part of the page
      - A sample of the rows in that dataset will show in a dialog
    - No more than 200 rows will be shown (to find out how to view more,
      see "Dataset usage" below)
    - You will not see information for columns for which you have no access
    - The dialog can scroll vertically *as well as* horizontally
 - Dataset usage
   - After clicking on a dataset from the dataset browser, click "Get Started" in the
     upper right part of the page
   - Up to four sections of sample code will be show (Spark, Hive, Python, and R)
   - Click on the tab of your choice and copy/paste the code into the associated
     environment to use this dataset outside of the Web UI

If you have all access to the dataset, you will also see an 'Access' tab where you can 
inspect which groups have access to which fields in the dataset. Clicking on the access
tab will show you a similar experience to what is available on the dataset search page. 
There will be a column with:
- A **multi-select box** where you can select a group or set of groups to see 
which fields in the dataset those groups have access to. Like the dataset access view,
only groups with some level of access to the dataset will be shown in this multi-select.
Any group not shown has no access.
- An **Apply** button that applies the currently selected set of groups to the access 
column view.
- **checkmark** or **lock** icons indicating whether the currently selected groups
have access to the field or not. 

Additionally, the access tab lets you compare up to three different sets of groups' access
at the same time by selecting different sets of groups in the three different access 
columns.


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
