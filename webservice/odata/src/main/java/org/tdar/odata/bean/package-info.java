/**
 * Provides OData Server support. 
 * 
 * OData is a standardised RESTful web service that provides CRUD and find operations to external clients.
 * It is the top component in a stack based on REST, ATOM, ATOMPUB and ODATA.
 * OData provides a BASIC-like query language, a domain model and data typing to this stack.
 * 
 * The code here is based on the odata4j libraries and example code.
 * The other Java implementation is Restlets.
 * 
 * The protocol comes out of Microsoft and has significant industry support.
 * There are are a number of server and client implementations in various languages such as DotNet and PHP.
 * 
 * Within tDAR it is intended to provide CRUD operations on DataTables.
 * It is hoped to be able to demonstrate interoperability between the odata4j server implementation implementation used here and the Excel Plug-in client.
 */
/**
 * @author Richard Rothwell
 *
 */
package org.tdar.odata.bean;