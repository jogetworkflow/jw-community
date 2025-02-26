<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader bodyCssClass=" builder-popup no-header" builderTheme="true"/>  
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tooltipster/js/tooltipster.bundle.min.js"></script>   
<div id="main-body-content">
    <div id="resources">
        <div id="resources-container" class="list-view">           
            <ui:jsontable url="${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/resource/list?${pageContext.request.queryString}"
                var="JsonResourcesDataTable"
                divToUpdate="ResourcesList"
                jsonData="data"
                rowsPerPage="15"
                width="100%"
                sort="id"
                desc="false"
                href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/resource/permission"
                hrefParam="id"
                hrefQuery="true"
                hrefDialog="true"
                hrefDialogTitle=""
                checkbox="true"
                checkboxButton1="console.app.resources.create.label"
                checkboxCallback1="addResource"
                checkboxOptional1="true"
                checkboxButton2="general.method.label.delete"
                checkboxCallback2="appResourceDelete"
                searchItems="filter|Filter"
                fields="['image','id','filesize','permissionClassLabel']"
                column1="{key: 'image', label: 'console.app.resource.common.label.preview', sortable: false, type: 'image'}"
                column2="{key: 'id', label: 'console.app.resource.common.label.id', sortable: true}"
                column3="{key: 'filesize', label: 'console.app.resource.common.label.filesize', sortable: true}"
                column4="{key: 'permissionClassLabel', label: 'console.app.resource.common.label.permission', sortable: false}" 
            /> 
        </div>                    
        <div id="resourcesGrid">
            <div id="main-content" class="file-manager">
                <div class="content">            
                    <div id="file-list" class="row clearfix"></div> 
                </div>
            </div>
        </div>
    </div>
    <div id="fileCount" class="file-count">   
        <span>0 Files</span>
    </div>
    <script>
        <ui:popupdialog var="resourceCreateDialog" src="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/resource/create"/>
        let jsonData = [];  
        $(document).ready(function() {
            
            const htmlContent = `
                <div class="view-toggle-group">
                    <input type="radio" id="listViewRadio" name="viewToggle" value="list" style="display: none;" checked>
                    <label for="listViewRadio" class="view-toggle-button">
                        <i class="fas fa-list"></i>
                    </label>
                    <input type="radio" id="gridViewRadio" name="viewToggle" value="grid" style="display: none;">
                    <label for="gridViewRadio" class="view-toggle-button">
                        <i class="fas fa-th"></i>
                    </label>
                </div>  
                <div class="tooltip">
                    <i class="fa fas fa-info-circle" id="tooltipIcon"></i>
                </div>
                <div class="group">   
                    <div class="property-input sort-field-group">
                        <label for="sortFieldDropdown">Sort by:</label>
                        <select id="sortFieldDropdown">
                            <option value="name"><ui:msgEscJS key="console.app.resource.common.label.name"/></option>
                            <option value="filesize"><ui:msgEscJS key="console.app.resource.common.label.filesize"/></option>
                        </select>
                        <div class="sort-toggle-group">
                            <input type="radio" id="sortAsc" name="sortToggle" value="asc" style="display: none;" checked="">
                            <label for="sortAsc" class="sort-toggle-button">
                                <i class="fas fa-sort-amount-up"></i>
                            </label>
                            <input type="radio" id="sortDesc" name="sortToggle" value="desc" style="display: none;">
                            <label for="sortDesc" class="sort-toggle-button">
                                <i class="fas fa-sort-amount-down"></i>
                            </label>
                        </div>
                    </div>
                </div>           
            `;
            $('#JsonResourcesDataTable_ResourcesList-search').append(htmlContent);
            $('#JsonResourcesDataTable_ResourcesList-search').css('display', 'ruby');   
            
            $('#tooltipIcon').tooltipster({
                content: $('<span><ui:msgEscJS key="console.app.resource.grid.label.tooltip"/></span>'),
                side: 'right',
                theme: 'tooltipster-light', 
                animation: 'fade',
                delay: 200
            });
   
            const lastView = localStorage.getItem('selectedView') || 'list'; 
            setView(lastView);  
            
            loadResources();

            $('input[name="viewToggle"]').on('change', function() {
            const selectedView = $(this).val(); 
            setView(selectedView); 

            localStorage.setItem('selectedView', selectedView); 
            });

            function setView(view) {
                if (view === 'list') {
                    $('#main-content').hide();
                    $('.flexigrid').css('display', 'block');
                    $('#resources-container').removeClass('grid-view').addClass('list-view');
                    $('.sort-field-group').hide(); 
                    $('.tooltip').hide(); 
                    $('#listViewRadio').prop('checked', true);  
                } else {
                    $('#resources-container').removeClass('list-view').addClass('grid-view');
                    $('#main-content').show();
                    $('.flexigrid').css('display', 'none');
                    $('.sort-field-group').show(); 
                    $('.tooltip').show(); 
                    $('#gridViewRadio').prop('checked', true);  
                    if (jsonData.length) {
                        renderFiles(jsonData);
                    }
                }
            }
                  
            function loadResources() {
                $.ajax({
                  url: "${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/resource/list?",
                  method: "GET",
                  dataType: "json",
                  success: function(response) {
                      jsonData = response.data; 
                      renderFiles(jsonData);
                  },
                      error: function(error) {
                      console.log("Error fetching data:", error);
                  }
                });
            }

            $('#JsonResourcesDataTable_searchTerm').hide();
            
            <c:if test="${protectedReadonly == 'true'}">
                $(".ui-tabs-panel button").hide();
            </c:if>
                               
            let selectedField = 'name'; 
            let selectedOrder = 'asc';  

            $('#sortFieldDropdown').on('change', function() {
                selectedField = $(this).val();
                sortData(selectedField, selectedOrder); 
            });

            $('input[name="sortToggle"]').on('change', function() {
                selectedOrder = $(this).val(); 
                sortData(selectedField, selectedOrder);
            });
        });
        
        
        //override filter to handle Resource Grid Filtering
        var org_filter = window.filter;
        var filter = function(jsonTable, url, value){
            org_filter(jsonTable, url, value);
            handleResourceGridFiltering();
        };
        function handleResourceGridFiltering() {
            var condition = $('#JsonResourcesDataTable_searchCondition').val().toLowerCase();

            if (typeof jsonData !== 'undefined' && Array.isArray(jsonData)) {
                var filteredData = jsonData.filter(function(item) {
                    return item.id.toLowerCase().includes(condition) || 
                           item.permissionClassLabel.toLowerCase().includes(condition) ||
                           item.filesize.toString().toLowerCase().includes(condition); 
                });

                if (typeof renderFiles === 'function') {
                    renderFiles(filteredData);
                }
            }
        }

        function sortData(field, order) {
            var sortedData = jsonData.slice().sort(function(a, b) {
                if (field === 'name') {
                    var nameA = a.id.toLowerCase();
                    var nameB = b.id.toLowerCase();
                    return order === 'asc' ? (nameA > nameB ? 1 : -1) : (nameA < nameB ? 1 : -1);
                } else if (field === 'filesize') {
                    return order === 'asc' 
                        ? fileSizeToBytes(a.filesize) - fileSizeToBytes(b.filesize)
                        : fileSizeToBytes(b.filesize) - fileSizeToBytes(a.filesize);
                }
            });
            renderFiles(sortedData);
        }
        
        function renderFiles(fileData) {
            var data = Array.isArray(fileData) ? fileData : [fileData];
            var fileList = $('#file-list');
            var gridContainer = $(".row");
            gridContainer.empty();  
            
            var fileCount = data.length;
            $('#fileCount span').text(fileCount + " Files");

            var currentFileIds = new Set(
                fileList.find('.file').map(function () {
                    return $(this).find('.file-name p').text();
                }).get()
            );

            var selectedRows = [];

            var selectedIdsDiv = $('#resourcesGrid').siblings('#resources-container').find('div#ResourcesList_selectedIds');
            if (!selectedIdsDiv.length) {
                console.log("Div with id 'ResourcesList_selectedIds' not found.");
                return;
            }

            $.each(data, function (index, file) {
                if (!currentFileIds.has(file.id)) {

                    var JsonResourcesDataTables = JsonResourcesDataTable;
                    var resourceUrl = JsonResourcesDataTables.link.href + "?id=" + file.id;

                    var colDiv = $('<div>').addClass('col-lg-3 col-md-4 col-sm-12');
                    var cardDiv = $('<div>').addClass('card');
                    var fileDiv = $('<div>').addClass('file');
                    var hoverDiv = $('<div>').addClass('hover');
                    var deleteButton = $('<button>')
                        .attr('type', 'button')
                        .addClass('btn btn-icon btn-danger')
                        .html('<i class="fa fa-trash"></i>');

                    hoverDiv.append(deleteButton);

                    if (isImageFile(file.image)) {
                        var imageDiv = $('<div>').addClass('file-img');
                        var img = $('<img>')
                            .attr('src', file.image)
                            .attr('alt', 'img')
                            .addClass('img-fluid img-container')
                            .on('error', function() {
                                if (!$(this).data('fallback')) {
                                    var fallbackSrc = file.image.replace('.webp', '.jpg'); 
                                    $(this).attr('src', fallbackSrc).data('fallback', true);  
                                } else {
                                    imageDiv.removeClass('file-img').addClass('icon');
                                    var iconClass = getIconClass(file.image);
                                    var icon = $('<i>').addClass('fas fa-unlink');
                                    imageDiv.append(icon); 
                                }
                            })
                            .on('load', function() {
                                var orientation = orientationDetection(this);
                                $(this).addClass(orientation);   
                                imageDiv.append(img);
                            });
                        fileDiv.append(imageDiv);
                    } else {
                        var iconDiv = $('<div>').addClass('icon');
                        var iconClass = getIconClass(file.image);
                        var icon = $('<i>').addClass(iconClass);
                                   iconDiv.append(icon);
                                   fileDiv.append(iconDiv);
                    }

                    var fileNameDiv = $('<div>').addClass('file-name');
                    var fileNameP = $('<p>').addClass('m-b-5 text-muted').text(file.id).attr('title', file.id);  
                    var fileDetailsSmall = $('<small>').text(file.filesize);  

                    fileNameDiv.append(fileNameP).append(fileDetailsSmall);
                    fileDiv.append(fileNameDiv);
                    cardDiv.append(fileDiv);
                    colDiv.append(cardDiv);
                    fileList.append(colDiv);  

                    $(cardDiv).on('click', function () {
                        if (selectedRows.includes(file.id)) {
                            $(this).css("outline", ""); 
                            selectedRows = selectedRows.filter(function (id) {
                                return id !== file.id;
                            });
                        } else {
                            $(this).css("outline", "2px solid var(--theme-primary-color-4)"); 
                            selectedRows.push(file.id);
                        }
                        selectedIdsDiv.html("," + selectedRows.join(","));
                    });

                    $(cardDiv).on('dblclick', function () {
                        var popupDialog = new PopupDialog(resourceUrl, "Resource Details");
                        popupDialog.init();
                    });
                }
            });
        }

        function orientationDetection(img) {
            if (img.naturalWidth > img.naturalHeight) {
                return 'landscape'; 
            } else if (img.naturalHeight > img.naturalWidth) {
                return 'portrait';  
            } else {
                return 'square';    
            }
        }
        
        function isImageFile(filePath) {
           var imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];
           var fileExtension = filePath.split('.').pop().toLowerCase();
           return imageExtensions.includes(fileExtension);
       }

        function getIconClass(filePath) {
            var extension = filePath.split('.').pop().toLowerCase();
            var iconMap = {
               'pdf': 'fa fa-file-pdf',
               'doc': 'fa fa-file-word',
               'docx': 'fa fa-file-word',
               'xls': 'fa fa-file-excel',
               'xlsx': 'fa fa-file-excel',
               'zip': 'fa fa-file-archive',
               'txt': 'fa fa-file-alt',
               'mp4': 'fa fa-file-video',
               'mp3': 'fa fa-file-audio',
               'ppt': 'fa fa-file-powerpoint',
               'pptx': 'fa fa-file-powerpoint',
               'html': 'fa fa-file-code',
               'js': 'fa fa-file-code',
               'css': 'fa fa-file-code',
               'default': 'fa fa-file'  
            };
           return iconMap[extension] || iconMap['default'];
       }

        function fileSizeToBytes(fileSize) {
            const units = ['B', 'KB', 'MB', 'GB', 'TB'];
            const regex = /([0-9.]+)\s*(B|KB|MB|GB|TB)/i;
            const match = fileSize.match(regex);

            if (match) {
                const value = parseFloat(match[1]);
                const unit = match[2].toUpperCase();
                const index = units.indexOf(unit);
                if (index !== -1) {
                    return value * Math.pow(1024, index);
                }
            }
            return 0;
        }

        function addResource() {
            resourceCreateDialog.init();                      
        }
      
        function reloadResources() {
            $.ajax({
                url: "${pageContext.request.contextPath}/web/json/console/app/${appId}/${appVersion}/resource/list?",
                method: "GET",
                dataType: "json",
                success: function(response) {
                    jsonData = response.data; 
                    renderFiles(jsonData);
                },
                error: function(error) {
                    console.log("Error fetching updated data:", error);
                }
            });
        }

        function appResourceDelete(selectedList){
            if (confirm('<ui:msgEscJS key="console.app.resource.delete.label.confirmation"/>')) {
                parent.UI.blockUI();
                var callback = {
                    success : function() {
                        filter(JsonResourcesDataTable, '&filter=', $('#JsonResourcesDataTable_searchCondition').val());
                        JsonResourcesDataTable.clearSelectedRows();
                        var gridContainer = $(".row");
                        gridContainer.empty();  
                        reloadResources();
                        parent.UI.unblockUI();

                        selectedList.forEach(function(item){
                            parent.window.CustomBuilder.showMessage(item + '<ui:msgEscJS key="console.app.message.delete.toast.message"/>', "success", true);
                        })
                    }
                }
                var request = ConnectionManager.post('${pageContext.request.contextPath}/web/console/app/<c:out value="${appId}"/>/${appVersion}/resource/delete', callback, 'ids='+selectedList);
            }
        }

        function closeDialog() {
            resourceCreateDialog.close();
            reloadResources(); 
        }      

        function reloadTable() {
          closeDialog();
          filter(JsonResourcesDataTable, '&filter=', $('#JsonResourcesDataTable_searchCondition').val());
      }
    </script>
</div>
<commons:popupFooter />
