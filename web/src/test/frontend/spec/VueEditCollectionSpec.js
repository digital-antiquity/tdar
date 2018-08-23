/* global jasmine,  describe, it, expect, setFixtures, beforeEach, afterEach */
const Vue = require("vue").default;
const TDAR = require("JS/tdar.master");

describe("Vue-edit-collection.js: Edit collection resources Vue", function() {
	var resources
	
    beforeEach(function() {
    	resources = [{
	        "title": "00-08: No Report",
	        "description": "A formal report was not written for this project.",
	        "url": "",
	        "resourceType": "DOCUMENT",
	        "date": 2000,
	        "activeLatitudeLongitudeBoxes": [],
	        "detailUrl": "/document/380513/00-08-no-report",
	        "urlNamespace": "document",
	        "resourceTypeLabel": "Document",
	        "id": 380513,
	        "status": "DRAFT"
	    }, {
	        "title": "00-14: No Report",
	        "description": "No report.",
	        "url": "",
	        "resourceType": "DOCUMENT",
	        "date": 2000,
	        "activeLatitudeLongitudeBoxes": [],
	        "detailUrl": "/document/380514/00-14-no-report",
	        "urlNamespace": "document",
	        "resourceTypeLabel": "Document",
	        "id": 380514,
	        "status": "DRAFT"
	    }, {
	        "title": "00-15: No Report",
	        "description": "No report.",
	        "url": "",
	        "resourceType": "DOCUMENT",
	        "date": 2000,
	        "activeLatitudeLongitudeBoxes": [],
	        "detailUrl": "/document/380515/00-15-no-report",
	        "urlNamespace": "document",
	        "resourceTypeLabel": "Document",
	        "id": 380515,
	        "status": "DRAFT"
	    }, {
	        "title": "00-24: No Report",
	        "description": "No report written. Cancelled project",
	        "url": "",
	        "resourceType": "DOCUMENT",
	        "date": 2000,
	        "activeLatitudeLongitudeBoxes": [],
	        "detailUrl": "/document/380517/00-24-no-report",
	        "urlNamespace": "document",
	        "resourceTypeLabel": "Document",
	        "id": 380517,
	        "status": "DRAFT"
	    }, {
	        "title": "00-72: No Report",
	        "description": "No report.",
	        "url": "",
	        "resourceType": "DOCUMENT",
	        "date": 2000,
	        "activeLatitudeLongitudeBoxes": [],
	        "detailUrl": "/document/380518/00-72-no-report",
	        "urlNamespace": "document",
	        "resourceTypeLabel": "Document",
	        "id": 380518,
	        "status": "DRAFT"
	    }, {
	        "title": "0000a.jpg",
	        "description": "The former Evans Shire is now largely incorporated in Bathurst Region, while the small remaining section, including Burraga, has been added to Oberon Shire.  In 1985, Hughes Trueman Ludlow were commissioned to prepare a Heritage Study of Evans Shire.  As an aspect of this study, Dr Aedeen Cremin and Professor Ian Jack undertook an inventory of industrial archaeological sites within the shire.\n\nThe area is primarily famous as the scene of several New South Wales gold-rushes in the 1850s, and continued to attract gold-seekers for many decades.  That part of Hill End, the most celebrated of the gold-mining areas, which is now owned by the National Parks and Wildlife Service, was excluded from the study, but areas adjacent to Hill End were recorded.  The less well-known gold-mines and processing plant in Evans Shire, some in remote locations, are exceptionally well preserved, with a great deal of equipment still in situ. \n\nAll were visited, photographed and assessed in 1985, including the highly significant remains at Chambers Creek, Pine Ridge, and Sofala and in the extensive hinterland of Wattle Flat.\n\nOther extractive industry of minerals flourished in Evans Shire: copper at Apsley, Cow Flat and Burraga; silver among the complex ores at the highly polluted site of Sunny Corner; and molybdenum during World War I at Mount Tennyson, where the smelter remains are unique and impressive.\n\nThe limestone and marble industry in the shire was also recorded at Caloola, Cow Flat, Limekilns and Mountain Creek Run, as well as a rare talcose plant at Bringellet.\n\nSome rural industry was included, such as the complex including a powder magazine at Freemantle, shearing sheds at Arkell, Bunnamagoo and Freemantle and a slab smithy at Killongbutta.\n\nOver 1,000 black and white prints and colour slides were taken of these sites by Ian Jack in 1985.  These constitute the only attempt at a comprehensive photographic survey of the region’s industrial archaeology. (Ian Jack, September 2013, St Andrew’s College, University of Sydney.)\n",
	        "url": "",
	        "resourceType": "IMAGE",
	        "date": 2013,
	        "detailUrl": "/image/412680/0000ajpg",
	        "urlNamespace": "image",
	        "resourceTypeLabel": "Image",
	        "id": 412680,
	        "status": "DRAFT"
	    }, {
	        "title": "0000b.jpg",
	        "description": "The former Evans Shire is now largely incorporated in Bathurst Region, while the small remaining section, including Burraga, has been added to Oberon Shire.  In 1985, Hughes Trueman Ludlow were commissioned to prepare a Heritage Study of Evans Shire.  As an aspect of this study, Dr Aedeen Cremin and Professor Ian Jack undertook an inventory of industrial archaeological sites within the shire.\n\nThe area is primarily famous as the scene of several New South Wales gold-rushes in the 1850s, and continued to attract gold-seekers for many decades.  That part of Hill End, the most celebrated of the gold-mining areas, which is now owned by the National Parks and Wildlife Service, was excluded from the study, but areas adjacent to Hill End were recorded.  The less well-known gold-mines and processing plant in Evans Shire, some in remote locations, are exceptionally well preserved, with a great deal of equipment still in situ. \n\nAll were visited, photographed and assessed in 1985, including the highly significant remains at Chambers Creek, Pine Ridge, and Sofala and in the extensive hinterland of Wattle Flat.\n\nOther extractive industry of minerals flourished in Evans Shire: copper at Apsley, Cow Flat and Burraga; silver among the complex ores at the highly polluted site of Sunny Corner; and molybdenum during World War I at Mount Tennyson, where the smelter remains are unique and impressive.\n\nThe limestone and marble industry in the shire was also recorded at Caloola, Cow Flat, Limekilns and Mountain Creek Run, as well as a rare talcose plant at Bringellet.\n\nSome rural industry was included, such as the complex including a powder magazine at Freemantle, shearing sheds at Arkell, Bunnamagoo and Freemantle and a slab smithy at Killongbutta.\n\nOver 1,000 black and white prints and colour slides were taken of these sites by Ian Jack in 1985.  These constitute the only attempt at a comprehensive photographic survey of the region’s industrial archaeology. (Ian Jack, September 2013, St Andrew’s College, University of Sydney.)\n",
	        "url": "",
	        "resourceType": "IMAGE",
	        "date": 2013,
	        "detailUrl": "/image/412423/0000bjpg",
	        "urlNamespace": "image",
	        "resourceTypeLabel": "Image",
	        "id": 412423,
	        "status": "DRAFT"
	    }, {
	        "title": "0000b.jpg",
	        "description": "The former Evans Shire is now largely incorporated in Bathurst Region, while the small remaining section, including Burraga, has been added to Oberon Shire.  In 1985, Hughes Trueman Ludlow were commissioned to prepare a Heritage Study of Evans Shire.  As an aspect of this study, Dr Aedeen Cremin and Professor Ian Jack undertook an inventory of industrial archaeological sites within the shire.\n\nThe area is primarily famous as the scene of several New South Wales gold-rushes in the 1850s, and continued to attract gold-seekers for many decades.  That part of Hill End, the most celebrated of the gold-mining areas, which is now owned by the National Parks and Wildlife Service, was excluded from the study, but areas adjacent to Hill End were recorded.  The less well-known gold-mines and processing plant in Evans Shire, some in remote locations, are exceptionally well preserved, with a great deal of equipment still in situ. \n\nAll were visited, photographed and assessed in 1985, including the highly significant remains at Chambers Creek, Pine Ridge, and Sofala and in the extensive hinterland of Wattle Flat.\n\nOther extractive industry of minerals flourished in Evans Shire: copper at Apsley, Cow Flat and Burraga; silver among the complex ores at the highly polluted site of Sunny Corner; and molybdenum during World War I at Mount Tennyson, where the smelter remains are unique and impressive.\n\nThe limestone and marble industry in the shire was also recorded at Caloola, Cow Flat, Limekilns and Mountain Creek Run, as well as a rare talcose plant at Bringellet.\n\nSome rural industry was included, such as the complex including a powder magazine at Freemantle, shearing sheds at Arkell, Bunnamagoo and Freemantle and a slab smithy at Killongbutta.\n\nOver 1,000 black and white prints and colour slides were taken of these sites by Ian Jack in 1985.  These constitute the only attempt at a comprehensive photographic survey of the region’s industrial archaeology. (Ian Jack, September 2013, St Andrew’s College, University of Sydney.)\n",
	        "url": "",
	        "resourceType": "IMAGE",
	        "date": 2013,
	        "detailUrl": "/image/412681/0000bjpg",
	        "urlNamespace": "image",
	        "resourceTypeLabel": "Image",
	        "id": 412681,
	        "status": "DRAFT"
	    }, {
	        "title": "0001a.jpg",
	        "description": "In 1985 Hughes Trueman Ludlow were commissioned to prepare a Heritage Study of Mudgee  Shire.  As an aspect of this study, Dr Aedeen Cremin and Professor Ian Jack undertook an inventory of industrial archaeological sites within the shire.\n\nThe area is primarily famous as the scene of a New South Wales gold-rush in the 1850s, and alluvial gold-seeking was succeeded by extensive reef-mining in the 1870s and beyond.  Gold-mines and processing plant around Hargraves and Windeyer were visited and recorded, but the town of Gulgong was excluded from the survey in 1985. \n\nMudgee is unusual in the quantity and quality of clay deposits, highly suitable for the manufacture both of bricks and clay-pipes..  The traditional brickworks at Mudgee, opened by the Roth family in the Edwardian period, and the surviving Scotch kilns in South Mudgee were extensively photographed. The surviving kaolin mine at Home Rule and the clay quarry and loader at Pipe Clay Creek near Buckaroo were also recorded.\n\nSome rural industry was included, such as the wineries at Erudgere and Fairview at Mudgee and station huts at Biraganbil and on the Triambil road and the ruins of a stone dairy at Wilbertree.\n\nOver 300 black and white prints and colour slides were taken of these sites by Ian Jack in 1985.  These constitute the only attempt at a photographic survey of the region’s industrial archaeology. (Ian Jack, September 2013, St Andrew’s College, University of Sydney.)\n",
	        "url": "",
	        "resourceType": "IMAGE",
	        "date": 2013,
	        "detailUrl": "/image/413364/0001ajpg",
	        "urlNamespace": "image",
	        "resourceTypeLabel": "Image",
	        "id": 413364,
	        "status": "DRAFT"
	    }, {
	        "title": "0001a.jpg",
	        "description": "The former Evans Shire is now largely incorporated in Bathurst Region, while the small remaining section, including Burraga, has been added to Oberon Shire.  In 1985, Hughes Trueman Ludlow were commissioned to prepare a Heritage Study of Evans Shire.  As an aspect of this study, Dr Aedeen Cremin and Professor Ian Jack undertook an inventory of industrial archaeological sites within the shire.\n\nThe area is primarily famous as the scene of several New South Wales gold-rushes in the 1850s, and continued to attract gold-seekers for many decades.  That part of Hill End, the most celebrated of the gold-mining areas, which is now owned by the National Parks and Wildlife Service, was excluded from the study, but areas adjacent to Hill End were recorded.  The less well-known gold-mines and processing plant in Evans Shire, some in remote locations, are exceptionally well preserved, with a great deal of equipment still in situ. \n\nAll were visited, photographed and assessed in 1985, including the highly significant remains at Chambers Creek, Pine Ridge, and Sofala and in the extensive hinterland of Wattle Flat.\n\nOther extractive industry of minerals flourished in Evans Shire: copper at Apsley, Cow Flat and Burraga; silver among the complex ores at the highly polluted site of Sunny Corner; and molybdenum during World War I at Mount Tennyson, where the smelter remains are unique and impressive.\n\nThe limestone and marble industry in the shire was also recorded at Caloola, Cow Flat, Limekilns and Mountain Creek Run, as well as a rare talcose plant at Bringellet.\n\nSome rural industry was included, such as the complex including a powder magazine at Freemantle, shearing sheds at Arkell, Bunnamagoo and Freemantle and a slab smithy at Killongbutta.\n\nOver 1,000 black and white prints and colour slides were taken of these sites by Ian Jack in 1985.  These constitute the only attempt at a comprehensive photographic survey of the region’s industrial archaeology. (Ian Jack, September 2013, St Andrew’s College, University of Sydney.)\n",
	        "url": "",
	        "resourceType": "IMAGE",
	        "date": 2013,
	        "detailUrl": "/image/412424/0001ajpg",
	        "urlNamespace": "image",
	        "resourceTypeLabel": "Image",
	        "id": 412424,
	        "status": "DRAFT"
	    }];
    });
    
    it("Add resources to the list", function(done) {
        // change the fixture path to point to our template
        jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/collection/";
        var html = jasmine.getFixtures().read("vue-edit-collection.html");
        var fix = jasmine.getFixtures().set(html);
        var vapp = TDAR.vuejs.editcollectionapp.init();

        expect(vapp.managedAdditions).toHaveLength(0);
        expect(resources.length).toBeGreaterThan(0);
        
        addManagedResources(vapp);
        addUnmanagedResources(vapp);
        
        expect(vapp.managedAdditions).toHaveLength(2);
        expect(vapp.unmanagedAdditions).toHaveLength(3);
        
        
        vapp.$forceUpdate();
        // make VueJS "tick" or re-render
        
        Vue.nextTick(function() {
        	var rows = fix.find("#tblToAdd tr");
        	//console.debug(rows);
        	console.log("There are "+rows.length+" rows ");
            //expect(rows).toHaveLength(5);
          	var row = rows[0];
          	expect(row).toContainText(resources[0]["title"]);
        });
        done();
        console.info("------------------------------------- vue ---------------------------------------");
    });
    
    it("Undo modifications from the pending changes", function(){
    	jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/collection/";
        var html = jasmine.getFixtures().read("vue-edit-collection.html");
        var fix = jasmine.getFixtures().set(html);
        var vapp = TDAR.vuejs.editcollectionapp.init();
    	
        var rows = fix.find("#tblToAdd tr");
      	var row = rows[0];
      	
      	addUnmanagedResources(vapp);
      	addManagedResources(vapp);
      	
    	vapp.undoModification(resources[0].id, true, true) ;
    	vapp.undoModification(resources[1].id, false, true) ;
    	
    	expect(vapp.managedAdditions).toHaveLength(1);
    	expect(vapp.managedAdditions[0].id).not.toEqual(resources[0].id);
    	
     	expect(vapp.unmanagedAdditions).toHaveLength(2);
    	expect(vapp.unmanagedAdditions[0].id).not.toEqual(resources[1].id);
    });
   
    
    function addUnmanagedResources(vapp){
        vapp.unmanagedAdditions.push(resources[1])
        vapp.unmanagedAdditions.push(resources[2])
        vapp.unmanagedAdditions.push(resources[3])
    }
    
    function addManagedResources(vapp){
        vapp.managedAdditions.push(resources[0])
        vapp.managedAdditions.push(resources[4])
    }

});