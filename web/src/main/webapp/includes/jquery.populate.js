/**
 * FIXME:  this jQuery plugin has been modified to support the indexed field naming we use in tDAR (see changelog). Don't upgrade unless these modifications
 * are duplicated or, preferebly, workarounds are made to make these modifications unnecessary.
 * 
 * Original location: http://www.keyframesandcode.com/resources/javascript/jQuery/demos/populate-demo.html
 */
jQuery.fn.populate = function(obj, options) {

        //escape jquery selector string
        function escapeJquerySelector(str) {
            return str.replace(/([ #;&,.+*~\':"!^$[\]()=>|\/])/g,'\\$1'); 
        }
    
	
	// ------------------------------------------------------------------------------------------
	// JSON conversion function
	
		// convert 
			function parseJSON(obj, path)
			{
				// prepare
					path = path || '';
				
				// iteration (objects / arrays)
					if(obj == undefined)
					{
						// do nothing
					}
					else if(obj.constructor == Object)
					{
						for(var prop in obj)
						{
							//jtd: adding support for 'struts' naming when using hierarchical json
							if(options.strutsNaming) {
								var name = path + (path == '' ? prop : '.' + prop );
							} else {
								var name = path + (path == '' ? prop : '[' +prop+ ']');
							}
							
							parseJSON(obj[prop], name);
						}
					}
						
					else if(obj.constructor == Array)
					{
						for(var i = 0; i < obj.length; i++)
						{
							var index	= options.phpIndices ? i : '';
							index		= options.phpNaming ? '[' +index+']' : index;
							//jtd: adding support to exclude specific fields from index notation via noIndicesFor
							var name = path;
							if(($.inArray(path, options.noIndicesFor) == -1)) {
								name = path + index;
							}
							parseJSON(obj[i], name);
						}
					}
					
				// assignment (values)
					else
					{
						// if the element name hasn't yet been defined, create it as a single value
						if(arr[path] == undefined)
						{
							arr[path] = obj;
						}
		
						// if the element name HAS been defined, but it's a single value, convert to an array and add the new value
						else if(arr[path].constructor != Array)
						{
							arr[path] = [arr[path], obj];
						}
							
						// if the element name HAS been defined, and is already an array, push the single value on the end of the stack
						else
						{
							arr[path].push(obj);
						}
					}
	
			};


	// ------------------------------------------------------------------------------------------
	// population functions
		
		function debug(str)
		{
			if(window.console && console.log && options.debug)
			{
				console.log(str);
			}
		}
		
		function getElementName(name)
		{
			if (!options.phpNaming)
			{
				name = name.replace(/\[\]$/,'');
			}
			
			return name;
		}
		
		function populateElement(parentElement, name, value)
		{
			var selector	= options.identifier == 'id' ? '#' + name : '[' +options.identifier+ '="' +name+ '"]';
			var element		= jQuery(selector, parentElement);
			value			= value.toString();
			value			= value == 'null' ? '' : value;
			element.html(value);
		}
		
		//jtd: modifications:  form can be a form element, or t can be html element that we will consider the 'scope' to limit which form elements to consider
		function populateFormElement(form, name, value)
		{

			// check that the named element exists in the form
				var name	= getElementName(name); // handle non-php naming
				//var element	= form[name];
				
				//escape for use in css selector
				name = escapeJquerySelector(name);
	
				
				var element = $('[name=' + name +']', form);
				
				//the rest of this routine expects a native array or single item
				element = $.makeArray(element);
				
			// if the form element doesn't exist, check if there is a tag with that id
				if(element == undefined || element.length===0) 
				{
					// look for the element
					element = jQuery('#' + name, form);
					if(element.length===0)
					{
					    return false;
					} else {
					    element = $.makeArray(element);
					}
					
				}
					
			// debug options				
				if(options.debug)
				{
					_populate.elements.push(element);
				}
				
			// now, place any single elements in an array.
			// this is so that the next bit of code (a loop) can treat them the 
			// same as any array-elements passed, ie radiobutton or checkox arrays,
			// and the code will just work

				elements = element.type == undefined && element.length ? element : [element];
				
				
			// populate the element correctly
			
				for(var e = 0; e < elements.length; e++)
				{
					
				// grab the element
					var element = elements[e];
					
				// skip undefined elements or function objects (IE only)
					if(!element || typeof element == 'undefined' || typeof element == 'function')
					{
						continue;
					}
					
				// anything else, process
					switch(element.type || element.tagName)
					{
	
						case 'radio':
							// use the single value to check the radio button
							element.checked = (element.value != '' && value.toString() == element.value);
							
						case 'checkbox':
							// depends on the value.
							// if it's an array, perform a sub loop
							// if it's a value, just do the check
							
							var values = value.constructor == Array ? value : [value];
							for(var j = 0; j < values.length; j++)
							{
								element.checked |= element.value == values[j];
							}
							
							//element.checked = (element.value != '' && value.toString().toLowerCase() == element.value.toLowerCase());
							break;
							
						case 'select-multiple':
							var values = value.constructor == Array ? value : [value];
							for(var i = 0; i < element.options.length; i++)
							{
								for(var j = 0; j < values.length; j++)
								{
									element.options[i].selected |= element.options[i].value == values[j];
								}
							}
							break;
						
						case 'select':
						case 'select-one':
							element.value = value.toString() || value;
							break;
	
						case 'text':
						case 'button':
						case 'textarea':
						case 'submit':
						default:
							value			= value == null ? '' : value;
							element.value	= value;
							
					}
						
				}

		}
		

		
	// ------------------------------------------------------------------------------------------
	// options & setup
		
		// exit if no data object supplied
			if (obj === undefined)
			{
				return this;
			};
		
		// options
			var options = jQuery.extend
			(
				{
					phpNaming:			true,
					phpIndices:			false,
					noIndicesFor:		[],
					resetForm:			true,
					identifier:			'id',
					debug:				false,
					strutsNaming:		false
				},
				options
			);
				
			if(options.phpIndices)
			{
				options.phpNaming = true;
			}
	
	// ------------------------------------------------------------------------------------------
	// convert hierarchical JSON to flat array
		
			var arr	= [];
			parseJSON(obj);
			
			if(options.debug)
			{
				_populate =
				{
					arr:		arr,
					obj:		obj,
					elements:	[]
				}
			}
	
	// ------------------------------------------------------------------------------------------
	// main process function
		
		this.each
		(
			function()
			{
				
				// variables
					var tagName	= this.tagName.toLowerCase();
					//var method	= tagName == 'form' ? populateFormElement : populateElement;
					
				// reset form?
					if(tagName == 'form' && options.resetForm)
					{
						this.reset();
					}

				// update elements
					for(var i in arr)
					{
					    populateFormElement(this, i, arr[i]);
					}
			}
			
		);

return this;
};