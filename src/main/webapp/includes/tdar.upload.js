/* ASYNC FILE UPLOAD SUPPORT */

function displayAsyncError(handler, msg) {
	var errorMsg = ("<p>tDAR experienced errors while uploading your file.  Please try again, or if the error persists, please save this record without the "
			+ " file attachment and notify a tDAR Administrator.</p>");
	if (msg)
		errorMsg = msg;
	var jqAsyncFileUploadErrors = $('#divAsyncFileUploadErrors');
	jqAsyncFileUploadErrors.html(errorMsg);
	jqAsyncFileUploadErrors.show();
	try {
		handler.removeNode(handler.uploadRow);
	} catch (ex) {
	}
	setTimeout(function() {
		jqAsyncFileUploadErrors.fadeOut(1000);
	}, 10000);
}

function sendFileIfAccepted(event, files, index, xhr, handler, callBack) {
    if (!files || files.length == 0) {
        handler.removeNode(handler.uploadRow);
        return false;
    }
    var accepted = false;
    var msgs = "";
    if (files[0].name) {
        var i = files.length - 1;
        while (i >= 0) {
            var accepted_ = fileAccepted(files[i].name);
            console.log(files[i].name + " : " + accepted_);
            if (accepted_ == false) {
                msgs += '<p>Sorry, this file type is not accepted:"'
                        + files[i].name + '"</p>';
                files.splice(i, 1);
            }
            ;
            i--;
        }
        if (files.length > 0) {
            accepted = true;
        }
    } else {
        if (index == undefined) {
            index = 0;
        }
        console.log(index + " : " + files);
        accepted = fileAccepted(files[index].name);
    }

    if (accepted) {
        asyncUploadStarted();
        callBack();
    }
    if (!accepted || msgs != "") {
        if (msgs == "") {
            msgs = '<p>Sorry, this file type is not accepted: "'
                    + files[index].name + '"</p>';
        }
        displayAsyncError(handler, msgs);
        return;
    }
}

function showAsyncReminderIfNeeded() {
	// if we have files in the downloaded table we don't need the reminder shown
	$('#reminder').show();
	if ($('#files').find('tr').length > 1)
		$('#reminder').hide();
}

// grab a ticket (if needed) from the server prior to yielding control to the
// file processor.
// FIXME: on-demand ticket grabbing only works when multiFileRequest turned on.
// Make it work even when turned off.

// additional form data sent with the ajax file upload. for now we only need the
// ticket id
function getFormData() {
	var frmdata = [ {
		name : 'ticketId',
		value : $('#ticketId').val()
	} ];
	// console.log("getFormData:" + frmdata);
	// console.log(frmdata);
	return frmdata;
}

function asyncUploadStarted() {
	g_asyncUploadCount++;
	formSubmitDisable();
}

function asyncUploadEnded() {
	g_asyncUploadCount--;
	if (g_asyncUploadCount <= 0) {
		formSubmitEnable();
	}
}

function initFileUpload(event, files, index, xhr, handler, callBack) {
	console.log('initFileUpload');
	if ($('#ticketId').val()) {
		sendFileIfAccepted(event, files, index, xhr, handler, callBack);
	} else {
		// grab a ticket and set the ticket id back to
		// the hidden form
		// field
		var ticketUrl = getBaseURI() + "upload/grab-ticket";
		$.post(ticketUrl, function(data) {
			$('#ticketId').val(data.id);
			sendFileIfAccepted(event, files, index, xhr, handler, callBack); // proceed
			// w/
			// upload
		}, 'json');
	}
}

function buildUploadRow(files, index) {
	console.log('building upload row');
	console.log(files);
	if (index) {
		// browser doesn't support multi-file uploads,
		// so this
		// callback called once per file
		return $('<tr><td>'
				+ files[index].name
				+ '<\/td>'
				+ '<td class="file_upload_progress"><div><\/div><\/td>'
				+ '<td class="file_upload_cancel">'
				+ '<button type="button" class="ui-state-default ui-corner-all" title="Cancel" onclick="return false;">'
				+ '<span class="ui-icon ui-icon-cancel">Cancel<\/span>'
				+ '<\/button><\/td><\/tr>');
	} else {
		// browser supports multi-file uploads
		return $('<tr><td> Uploading '
				+ files.length
				+ ' files<\/td>'
				+ '<td class="file_upload_progress"><div><\/div><\/td>'
				+ '<td class="file_upload_cancel">'
				+ '<button type="button" class="ui-state-default ui-corner-all" title="Cancel" onclick="return false;">'
				+ '<span class="ui-icon ui-icon-cancel">Cancel<\/span>'
				+ '<\/button><\/td><\/tr>');
	}

}

function buildDownloadRow(jsonObject) {
	var $files = $("#files");
	if ($files == undefined || $files.length == 0)
		return;
	
	$(".noFiles",$files).hide();
	var existingNumFiles = $('tbody tr',$files).not('.noFiles').length;
	if (existingNumFiles > 1 || jsonObject.files.length > 1) {
		$(".reorder",$files).show();
	}
	var toReturn = "";
	console.debug("Existing number of files: " + existingNumFiles);

	$("button.replaceButton.ui-state-disabled").each(function() {
		var $this = $(this);
		$this.removeClass("ui-state-disabled");
		$this.addClass("ui-state-default");
		$this.removeAttr('disabled');
	});

	for ( var fileIndex = 0; fileIndex < jsonObject.files.length; fileIndex++) {
		var row = $('#queuedFileTemplate').clone();
		var nextIndex = $(formId).data('nextFileIndex');
		console.debug("next file index: " + nextIndex);
		row.find('*').each(
				function() {
					var elem = this;
					// skip any tags with the repeatRowSkip attribute
					$.each([ "id", "onclick", "name", "for", "value" ],
							function(i, attrName) {
								// ensure each download row has a unique index.
								replaceAttribute(elem, attrName, '{ID}',
										nextIndex);
							});

					$.each([ "value", "onclick" ], function(i, attrName) {
						replaceAttribute(elem, attrName, '{FILENAME}',
								jsonObject.files[fileIndex].name);
					});
					// nodeType=3 is hardcoded for "TEXT" node
					if ($(this).contents().length == 1
							&& $(this).contents()[0].nodeType == 3) {
						var txt = $(this).text();
						if (txt.indexOf("{FILENAME}") != -1) {
							$(this).text(
									$(this).text().replace(/\{FILENAME\}/g,
											jsonObject.files[fileIndex].name));
						}
						if (txt.indexOf("{FILESIZE}") != -1) {
							$(this).text(
									$(this).text().replace(/\{FILESIZE\}/g,
											jsonObject.files[fileIndex].size));
						}
					}
					$(formId).data('nextFileIndex', nextIndex + 1);
				});

		toReturn += $(row).find("tbody").html();
	}
	return $(toReturn);
}
function applyAsync(formId) {
	$(formId).data('nextFileIndex', $('#files tr').not('.noFiles').length);
	//console.log("apply async called");
	$(formId).fileUploadUI({
		multiFileRequest : true,
		// FIXME: parts of this code aren't prepared for request-per-file
		// uploads yet.
		// dropZone:$('#divAsycFileUploadDropzone'),
		uploadTable : $('#uploadFiles'),
		fileInputFilter : $('#fileAsyncUpload'),
		downloadTable : $('#files'),
		url : getBaseURI() + "upload/upload",
		beforeSend : initFileUpload,
		formData : getFormData,
		buildUploadRow : buildUploadRow,
		buildDownloadRow : buildDownloadRow,
		onComplete : function(event, files, index, xhr, handler) {
			showAsyncReminderIfNeeded();
			asyncUploadEnded();
			applyWatermarks(document);
			// FIXME: onError registration doesn't appear to be called even when
			// status <> 200;
			if (xhr.status && xhr.status != 200) {
				displayAsyncError(handler);
			}
			console.log("complete");
		},
		onError : function(event, files, index, xhr, handler) {
			asyncUploadEnded();
			// For JSON parsing errors, the load event is saved as
			// handler.originalEvent:
			if (handler.originalEvent) {
				/* handle JSON parsing errors ... */
				displayAsyncError(handler + " " + $(xhr));
				console.error("json parsing error");
				console.error(event);
				console.log(handler);
				console.log(files);
			} else {
				/* handle XHR upload errors ... */
				displayAsyncError(handler);
				console.error("xhr upload error");
				console.error(event);
			}
		},
		onAbort : function(event, files, index, xhr, handler) {
			asyncUploadEnded();
			handler.removeNode(handler.uploadRow);
		}

	});

	// Ensure that the ticketid is blank if there are no pending file uploads.
	// For example, a user begins uploading their first file,
	// but then either cancels the upload or the upload terminates abnormally.
	$(formId).submit(function() {
		if ($('tr', '#files').not('.noFiles').size() == 0) {
			$('#ticketId').val('');
		}
	});

	console.log("apply async done");
}

function updateFileAction(rowId, value) {
	if (!value) {
		value = "MODIFY_METADATA";
	}
	var fileActionElement = $(rowId + " .fileAction");
	if (value == "MODIFY_METADATA") {
		var existingValue = fileActionElement.val();
		// no-op if it is an ADD/DELETE/REPLACE
		if ($.inArray(existingValue, [ "ADD", "DELETE", "REPLACE" ]) >= 0) {
			return;
		}
	}
	fileActionElement.val(value);
}

function deleteFile(rowId, newUpload, self) {
	console.log("deleteFile called" + rowId + " : " + newUpload);
	var buttonText = $(self).find('.ui-button-text');
	// console.debug("button text is: " + buttonText.html());
	var fileAction = $(rowId + " .fileAction");
	if (buttonText.html() == 'delete') {
		buttonText.html('undelete');
		$(rowId + " .filename").addClass('deleted-file');
		$(fileAction).attr("prev", fileAction.val());
		fileAction.val(newUpload ? "NONE" : "DELETE");
	} else {
		buttonText.html('delete');
		$(rowId + " .filename").removeClass('deleted-file');
		$(fileAction).val(fileAction.attr("prev"));
	}
	var $files = $("#files");
	var existingNumFiles = $('tbody tr',$files).not('.noFiles').length;
	if (existingNumFiles == 0) {
		$(".noFiles",$files).show();
		$(".reorder",$files).hide();
	}
	if (existingNumFiles < 2) {
		$(".reorder",$files).hide();
	}
}

function replaceFile(rowId, replacementRowId) {
	var row = $(rowId);
	var existingFilename = row.find(".filename").html();
	var replacementFilename = $(replacementRowId).find('.replacefilename')
			.html();
	// message to let the user know that this file is being used to replace an
	// existing file.
	$(replacementRowId)
			.find("td:first")
			.append(
					"<div class='ui-state-default'><span class='tdar-ui-icon ui-icon ui-icon-info'></span>Replacing <b>"
							+ existingFilename
							+ "</b> with <b>"
							+ replacementFilename + "</b>.</div>");
	// FIXME: simplify this logic through the use of effective CSS classes
	// clears out the delete button on the replacement row for the pending file
	$(replacementRowId).find("td:last").html("");
	// clear out all name attributes for the FileProxy hidden inputs on the
	// replacement row
	$(replacementRowId).find("input").removeAttr("name");
	// clear out the replacement row's confidential checkbox div
	$(replacementRowId).find(".proxyConfidentialDiv").html("");
	row.find(".fileAction").val("REPLACE");
	// set the replacement filename on the existing FileProxy
	row.find(".fileReplaceName").val(replacementFilename);
	row
			.find("td:first")
			.append(
					"<div class='ui-state-default'><span class='tdar-ui-icon ui-icon ui-icon-info'></span>Replacing with <b>"
							+ replacementFilename + "</b></div>");
	var table = row.parent();
	table.find(".fileSequenceNumber").each(function(index) {
		$(this).val(index);
	});
}

function replaceDialog(rowId, filename) {
	var contents = "<b>Select the Newly Uploaded File That Should Replace The Existing File:</b><br/><ul>";
	var replacementFiles = $('#files .newrow');
	if (replacementFiles.length == 0) {
		contents += "<li>Please upload a file and then choose the replace option</li>";
	}
	replacementFiles.each(function(i) {
		var replacementRowId = "#" + $(this).attr("id");
		var filename = $(this).find('.replacefilename').html();
		contents += "<li><input type='radio' name='replaceWith' value='"
				+ replacementRowId + "'><span>" + filename + "</span></li>";
	});

	contents += "</ul>";
	var $dialog = $('<div />')
			.html(contents)
			.dialog(
					{
						title : 'Replace File',
						buttons : {
							'replace' : function() {
								replaceFile(
										rowId,
										$(
												"input:radio[name=replaceWith]:checked")
												.val());
								$(this).dialog('close');
							},
							'cancel' : function() {
								$(this).dialog('close');
							}
						}
					});
}

function customSort(element) {
	var $table = $(element).parents("table").first();
	if ($(element).html() == 'Custom') {
		$table.find("tbody").sortable("enable");
		$table.addClass("enabled");
		$(element).html("Done");
	} else {
		$table.find("tbody").sortable("disable");
		$table.addClass("disabled");
        $table.removeClass("enabled");
		$(element).html("Custom");
	}
}

function setupSortableTables() {
	$('table.sortable tbody').sortable(
			{
				placeholder : "sortable-placeholder",
				disabled : true,
				change : function(event, ui) {
					console.log('sort update');
					$('.sortable-placeholder').children().remove();
					$('.sortable-placeholder').append(
							'<td  class="fileinfo" colspan="2">&nbsp</td>')
				},
				update : function(event, ui) {
					console.log('sort stop');
					$filesTable = ui.item.parent();
					// update every proxy's sequencenumberdd
					$filesTable.find(".fileSequenceNumber").each(
							function(index) {
								$(this).val(index);
							});

					// only update action of those proxies that have NONE action
					// (some might have ADD, REPLACE, etc.)
					$('.fileAction[value=NONE]', $filesTable).val(
							'MODIFY_METADATA');
				}
			});

}
