TDAR.vuejs.collectionwidget = (function(console, $, ctx, Vue,axios) {
    "use strict";

var _getSelectizeOpts = function() {
   var opts = {
        valueField: 'id',
        labelField: 'name',
        searchField: 'name',
        create: true,
        createOnBlur:true,
        render: {
            option: function(item, escape) {
                    return '<div>' +
                        '<span class="title">' +
                            '<span class="name">' + escape(item.name) + '</span>' +
                        '</span>' +
                    '</div>';
                }
            },
       
    load: function(query, callback) {
        if (!query.length) return callback();
        $.ajax({
            url: '/api/lookup/collection',
            type: 'GET',
            data: { 
               'term':query,
               'permission':'ADMINISTER_COLLECTION'
            },
            error: function() {
                callback();
            },
            success: function(res) {
                callback(res.collections);
            }
         });
     }
     }; 
     return opts;

}

var _init = function(appId) {

    Vue.component('collection', 
    { 
    props: ["id","name"],
    template: "<option :id='id' class='extra'>{{name}}</option>",
        computed: {
            ellipseName: function(){
            name
                // this needs to have the TDAR libs in so that the name will
				// compute.
               // TDAR.common.htmlEncode(TDAR.ellipsify(name, 80));
            }
        }
    });

     Vue.component('resource-collection', 
    { 
    props: ["id","name"],
    template: "<li :id='id' class='extra'>{{name}}</li>",
    });  

    $("#addToExisting").popover({placement:'right', delay:{hide:2000}});
        
        
      var app2 = new Vue({
        el: appId,
    
    data: { 
        items:[{id:"1",name:"Sample"}], 
        selectedCollection: 0 ,
        pick:"existing",
        options:[],
        newCollectionName:"",
        newCollectionDescription:"",
        showPermission:false,
        managedResource: false,
        resourceId: -1,
        canEdit: false,
        collections: {managed:[], unmanaged:[]}
    },
    
    mounted: function() {
        var $e = $(this.$el);
        if($e.data('resourceId')!=null){
	        Vue.set(this, 'canEdit',$e.data('canEdit'));
		    Vue.set(this, 'resourceId',$e.data('resourceId'));
		    this._getCollectionsForResource();
        }
    },
    
    methods: {

        _resetForm: function(){
            this.selectedCollection='',
            this.pick="existing",
            this.newCollectionName="";
            this.newCollectionDescription="";
            this.managedResource = false;
            var $select = $('#collection-list').selectize();
            $select[0].selectize.clear();
        },
        getCollections: function(){
            var self = this;
            axios.get("/api/lookup/collection?permission=ADMINISTER_COLLECTION").then(function(res) {
                    self.items = res.data;
            });
        },
        
        _addResultsToCollection: function(collectionId){
        	var url = $(this.$el).data("url");
        	console.log(url);
        	
        	axios.post("/api/search/"+url, {collectionId: collectionId}).then(function(res) {
        		console.log("Finished adding!!");
        	});
        },
        
        _addResourceToCollection : function(collectionId){
            var self=this;
            var data =  {
                "resourceId":self.resourceId,
                "collectionId":collectionId,
                "addAsManagedResource":this.managedResource
            }
            
            console.debug("Adding resource to collection");
            console.debug(data);
            axios.post('/api/collection/addtocollection', Qs.stringify(data)).then(function(){
                self._getCollectionsForResource();
                self._resetForm();
            }
            ).catch(function(res){
                console.log("An error occurred when adding to collection"); 
                console.log(res);
            });
            
        },
        
        _getCollectionsForResource : function(){
            var self = this;
            var data = {
                resourceId : this.resourceId
            }
            axios.get("/api/collection/resourcecollections?resourceId="+this.resourceId).then(function(response) {
                Vue.set(self,'collections',response.data);
            }).catch(function(error){
                console.error("An error ocurred getting a list of collections for this resource");
                console.error(error);
            });
        },
        
        ellipse : function(value){
           return TDAR.common.htmlEncode(TDAR.ellipsify(value, 80))
        }, 
        
        removeResourceFromCollection: function(collection,section){
        var data =  {
                resourceId:this.resourceId,
                collectionId:collection.id,
                type:section
            }
            var self = this;
            console.debug("removing collection id"+collection);
            axios.post('/api/collection/removefromcollection', Qs.stringify(data)).then(function(){
             self._getCollectionsForResource();
            }).catch(function(error){
                console.error("couldn't remove item from collection");
                console.debug(error);
            });
        },
        
        addToCollection:function(){
            var vapp = this;
            if(this.selectedCollection==""){
                $("#addToNew").popover('show');
                setTimeout(function () {
                       $('#addToNew').popover('hide');
                   }, 2000);
              }

            else if (isNaN(this.selectedCollection)) {
                    // post to create a new collection.
                    var data = {
                        collectionName: this.selectedCollection,
                        collectionDescription : this.newCollectionDescription
                    }
                    
                    // On success, add the resource.
                    axios.post('/api/collection/newcollection',Qs.stringify(data)).then(function(res){
                            console.log("New Collection added");
                            var id = res.data.id;
                            console.debug("new collection id is "+id);
                            vapp._addResourceToCollection(id);
                            vapp._resetForm();
                        }
                    );
                } else if(this.selectedCollection < 1){
                    // This should change the background color to red and
					// invalidate the box.
                    $("#collection-list").addClass("invalid-feedback");
                    
                    
                    console.debug("Adding popover");
                    // Change this to popovers.
                    $("#addToExisting").popover('show');
                     setTimeout(function () {
                            $('#addToExisting').popover('hide');
                        }, 2000);
                }
                else { 
                   vapp._addResourceToCollection(this.selectedCollection);
                   vapp._getCollectionsForResource();
                }
        },
        
        showGrant: function(){
           var index = $('#collection-list').prop('selectedIndex');
           if(index > 0 ){
            this.showPermission = this.items[index-1].owned==true;
           }
           else {
            this.showPermission = false;
           }
        },
        
        
        saveResultsToCollection: function(){
        	var vapp = this;
        	if(this.selectedCollection==""){
                $("#addToNew").popover('show');
                setTimeout(function () {
                       $('#addToNew').popover('hide');
                   }, 2000);
            }

            else if (isNaN(this.selectedCollection)) {
                    // post to create a new collection.
                    var data = {
                        collectionName: this.selectedCollection,
                        collectionDescription : this.newCollectionDescription
                    }
                    
                    // On success, add the resource.
                    axios.post('/api/collection/newcollection',Qs.stringify(data)).then(function(res){
                            console.log("New Collection added");
                            var id = res.data.id;
                            console.debug("new collection id is "+id);
                            vapp._addResultsToCollection(id);
                        }
                    );
                }
            else { 
                vapp._addResultsToCollection(this.selectedCollection);
             }
        }
        
    }
    });
      return app2;
    
    
}

return {
    init: _init,
    collectionSelectizeOptions : _getSelectizeOpts,
    main : function() {
        var appId = '#add-resource-form';
        if ($(appId).length  >0) {
            TDAR.vuejs.collectionwidget.init(appId);
        }
    }
}
})(console, jQuery, window, Vue,axios);
