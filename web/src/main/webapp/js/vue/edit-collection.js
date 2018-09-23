/** TDAR edit-collection.js */


const Vue = require("vue/dist/vue.esm.js").default;
// const axios = require("axios");
const datatable = require("./../tdar.datatable");
const common = require("./../tdar.common");
const core = require("./../tdar.core");


var _init = function(params) {
	var enableUnmanagedCollections = false;
	
	if(params!= null && params.enableUnmanagedCollections==true){
		enableUnmanagedCollections  = true;
	};
	
	var vm = new Vue(
			{
				el : '#editCollectionApp',
				data : {
					managedAdditions : [],
					managedRemovals : [],
					unmanagedAdditions : [],
					unmanagedRemovals : [],
					enableUnmanagedCollections: enableUnmanagedCollections
				},
				
				mounted : function() {
					console.log("enableUnmanagedCollections", this.enableUnmanagedCollections);
				},

				computed : {
					pendingRemovals : function() {
						return this.managedRemovals.length
								+ this.unmanagedRemovals.length;
					},

					pendingAdditions : function() {
						return this.managedAdditions.length
								+ this.unmanagedAdditions.length;
					},
					
					pendingAdditionText :function(){
						var p = this.pendingAdditions;
						
						if(p<=1){
							return "resource";
						}
						else {
							return p+" resources";
						}
					}, 
					
					pendingRemovalText :function(){
						var p = this.pendingRemovals;
						
						if(p<=1){
							return "resource";
						}
						else {
							return p+" resources";
						}
					}
					
					
				},

				methods : {
					ellipse : function(value) {
						return common.htmlEncode(core.ellipsify(value, 80))
					},

					search : function(value, array) {
						for (var i = 0; i < array.length; i++) {
							if (array[i].id == value) {
								console.debug("Found value " + value
										+ " at position " + i);
								return i;
							}
						}
						return -1;
					},

					removeFromArray : function(id, array) {
						var idx = this.search(id, array);
						if (idx !== -1) {
							console.debug("Removing " + id + " at  " + idx);
							array.splice(idx, 1);
						}
					},

					undoModification : function(id, isManaged, isAddition) {
					    console.debug("edit-collection:undoModification called");
					    var tableId = !isAddition ? '#existing_resources_datatable' : '#resource_datatable';
					    
					    console.debug("Table id is : "+tableId);
						var $dataTable = $(tableId);
						
						
						/**
                         * Verify that the datatable actually returns the right object. If it doesn't then it probably means that the DOM isn't properly
                         * loaded.
                         * 
                         */
						
						// console.debug("Data table is ");
						// console.debug($dataTable);
							
 
							if (isManaged) {
								if (isAddition) {
									console.debug("Removing " + id + " from managed additions");
								this.removeFromArray(id, this.managedAdditions)
							} else {
								console.debug("Removing " + id + " from managed removals");
								this.removeFromArray(id, this.managedRemovals)
							}
						} else {
							if (isAddition) {
								console.debug("Removing " + id
										+ " from unmanaged additions");
								this.removeFromArray(id, this.unmanagedAdditions)
							} else {
								console.debug("Removing " + id +" from unmanaged removals");
								this.removeFromArray(id, this.unmanagedRemovals)
							}
						}

						try {
						    datatable.removePendingChange(parseInt(id), isManaged, isAddition, $dataTable);
						} catch(e){
						    console.warn(e);
					    }
					}
				}
			});
	
	return vm;
}

module.exports = {
	init : _init,
	main : function() {
			TDAR.vuejs.editcollectionapp.init(appId);
	}
}

