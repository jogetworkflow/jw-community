<h3>@@pwa.offline.title@@</h3>
<h4>@@pwa.offline.desc@@</h4>

<div id="pwaOffline" class="form-data-container dataList">
    <table class="table xrounded_shadowed" style="width: 100%">
        <thead>
            <tr>
                <th data-breakpoints="xs">@@pwa.offline.id@@</th>
                <th>@@pwa.offline.userviewAppId@@</th>
                <th data-breakpoints="sm md">@@pwa.offline.formName@@</th>
                <th data-breakpoints="sm">@@pwa.offline.username@@</th>
                <th>@@pwa.offline.status@@</th>
                <th data-breakpoints="xs">@@pwa.offline.dateSubmitted@@</th>
                <th class="row_action"></th>
            </tr>
        </thead>
        <tbody>

        </tbody>
    </table>
</div>

<br/>
<br/>
<a href="${home_page_link!}">&laquo;@@pwa.offline.goBack@@</a>

<script type="text/javascript" src="${params.contextPath}/js/footable/responsiveTable.js?build=${build_number}"></script>

<script>
    $(document).ready(function(){
        var formDb;
        var DB_NAME = 'joget_${appId}-${userview.properties.id}';
        var FORM_DB_STORE_NAME = 'offline_post';

        var savedRequestsCache = {};

        function openDatabase() {
            var indexedDBOpenRequest = indexedDB.open(DB_NAME);

            indexedDBOpenRequest.onerror = function (error) {
                console.error('IndexedDB error:', error);
            }

            indexedDBOpenRequest.onupgradeneeded = function () {
                this.result.createObjectStore(FORM_DB_STORE_NAME, {
                    autoIncrement: true,
                    keyPath: 'id'
                });
            }

            indexedDBOpenRequest.onsuccess = function () {
                formDb = this.result;

                loadFormData();
            }
        }

        function getObjectStore(storeName, mode) {
            return formDb.transaction(storeName, mode).objectStore(storeName);
        }

        function savePostRequest(url, payload) {
            var request = getObjectStore(FORM_DB_STORE_NAME, 'readwrite').add({
                url: url,
                payload: payload,
                method: 'POST'
            });

            request.onsuccess = function (event) {
                console.log('a new post_request has been added to indexedb');
                formData = null;
            }

            request.onerror = function (error) {
                console.error(error)
            }
        }

        function loadFormData(){
            console.log('loadFormData');

            $('table tbody').html('');

            var savedRequests = [];
            var req = getObjectStore(FORM_DB_STORE_NAME).openCursor();

            req.onsuccess = async function(event) {
                var cursor = event.target.result;

                if (cursor) {
                    savedRequests.push(cursor.value);
                    cursor.continue();

                } else {
                    for(let savedRequest of savedRequests) {
                        savedRequestsCache[savedRequest.id] = savedRequest;

                        var formDataObj = new FormData();
                        var payload = savedRequest.payload;

                        for(var key in payload){
                            //check if File array
                            if(Array.isArray(payload[key]) && payload[key][0] instanceof File){
                                for(var i in payload[key]){
                                    formDataObj.append(key, payload[key][i]);
                                }
                            }else{
                                formDataObj.append(key, payload[key]);
                            }
                        }

                        var date = new Date(savedRequest.timestamp);
                        var dateString = formatDate(date);
                        //console.log(formatted_date)

                        var statusString = '@@pwa.offline.status.pending@@';
                        if(savedRequest.status === 1){
                            statusString = '@@pwa.offline.status.success@@';
                        }else if(savedRequest.status === 2){
                            statusString = '@@pwa.offline.status.fail@@';
                        }else if(savedRequest.status === 3){
                            statusString = '@@pwa.offline.status.formValidationError@@ (<a href="#" onclick="submitForm(' + savedRequest.id + ')">@@pwa.offline.view@@</a>)';
                        }

                        var $row = '<tr>'
                                 + '<td>' + savedRequest.id + '</td>'
                                 + '<td>' + savedRequest.userviewAppId + '</td>'
                                 + '<td>' + savedRequest.title + '</td>'
                                 + '<td>' + savedRequest.username + '</td>'
                                 + '<td>' + statusString + '</td>'
                                 + '<td>' + dateString + '</td>'
                                 + '<td class="row_action"><span class="row_action_inner"><a href="#" onclick="deleteForm(' + savedRequest.id + ')">delete</a></span></td>'
                                 + '</tr>';
                        $('table tbody').prepend($row);
                    }
                }
            }
        }

        function formatDate(date) {
            var hours = date.getHours();
            var minutes = date.getMinutes();
            var ampm = hours >= 12 ? 'pm' : 'am';
            hours = hours % 12;
            hours = hours ? hours : 12; // the hour '0' should be '12'
            minutes = minutes < 10 ? '0' + minutes : minutes;
            var strTime = hours + ':' + minutes + ' ' + ampm;
            return date.getDate() + "/" + (date.getMonth() + 1) + "/" + date.getFullYear() + "  " + strTime;
        }

        function createInput(form, inputName, inputValue){
            var element = document.createElement("input");
            element.type = "hidden";
            element.name = inputName;
            element.value = inputValue;
            form.appendChild(element);
        }

        window.deleteForm = function(id){
            if(confirm('@@pwa.offline.delete.confirm@@')){
                var objectStoreRequest = getObjectStore(FORM_DB_STORE_NAME, 'readwrite').delete(id);
                objectStoreRequest.onsuccess = function(event) {
                    loadFormData();
                };
            }
        }

        window.submitForm = function(id) {
            var savedRequest = savedRequestsCache[id];

            var form = document.createElement("form");

            form.method = "POST";
            form.action = replaceUrlParam(savedRequest.url, 'OWASP_CSRFTOKEN', ConnectionManager.tokenValue);

            for(var key in savedRequest.payload){
                if(key === 'OWASP_CSRFTOKEN'){
                    createInput(form, key, ConnectionManager.tokenValue);
                }else{

                    if(Array.isArray(savedRequest.payload[key])){
                        for(var i in savedRequest.payload[key]){
                            createInput(form, key, savedRequest.payload[key][i]);
                        }
                    }else{
                        createInput(form, key, savedRequest.payload[key]);
                    }
                }
            }

            document.body.appendChild(form);

            //delete the savedRequest
            getObjectStore(FORM_DB_STORE_NAME, 'readwrite').delete(savedRequest.id);

            var submitFormFunction = Object.getPrototypeOf(form).submit;
            submitFormFunction.call(form);
        }

        function replaceUrlParam(url, paramName, paramValue){
            if (paramValue === null) {
                paramValue = '';
            }
            var pattern = new RegExp('\\b(' + paramName + '=).*?(&|#|$)');
            if (url.search(pattern) >= 0) {
                return url.replace(pattern, '$1' + paramValue + '$2');
            }
            url = url.replace(/[?#]$/, '');
            return url + (url.indexOf('?') > 0 ? '&' : '?') + paramName + '=' + paramValue;
        }

        navigator.serviceWorker.addEventListener('message', function(event) {
            if(event.data.type !== undefined && (event.data.type === 'syncFailed' || event.data.type === 'syncSuccess')){
                loadFormData();
            }
        });

        openDatabase();        
    })
</script>