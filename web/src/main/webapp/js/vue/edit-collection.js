TDAR.vuejs.editcollectionapp = (function(console, $, ctx, Vue, axios) {
	"use strict";

	var _init = function() {
		var vm = new Vue(
				{
					el : '#editCollectionApp',
					data : {
						managedAdditions : [],
						managedRemovals : [],
						unmanagedAdditions : [],
						unmanagedRemovals : []
					},
					mounted : function() {

					},

					computed : {
						pendingRemovals : function() {
							return this.managedRemovals.length
									+ this.unmanagedRemovals.length;
						},

						pendingAdditions : function() {
							return this.managedAdditions.length
									+ this.unmanagedAdditions.length;
						}
					},

					methods : {
						ellipse : function(value) {
							return TDAR.common.htmlEncode(TDAR.ellipsify(value,
									80))
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
							var $dataTable = !isAddition ? $('#existing_resources_datatable')
									: $('#resource_datatable');

							if (isManaged) {
								if (isAddition) {
									console.debug("Removing " + id
											+ " from managed additions");
									this.removeFromArray(id,
											this.managedAdditions)
								} else {
									console.debug("Removing " + id
											+ " from managed removals");
									this.removeFromArray(id,
											this.managedRemovals)
								}
							} else {
								if (isAddition) {
									console.debug("Removing " + id
											+ " from unmanaged additions");
									this.removeFromArray(id,
											this.unmanagedAdditions)
								} else {
									console.debug("Removing " + id
											+ " from unmanaged additions");
									this.removeFromArray(id,
											this.managedRemovals)
								}
							}

							TDAR.datatable.removePendingChange(parseInt(id),
									isManaged, isAddition, $dataTable);
						}
					}
				});
		
		return vm;
	}

	return {
		init : _init,
		main : function() {
				TDAR.vuejs.editcollectionapp.init(appId);
		}
	}
})(console, jQuery, window, Vue, axios);
