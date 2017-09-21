(function () {

    'use strict';

    var root = this, $ = root.jQuery;

    if (typeof VOA === 'undefined') {
        root.VOA = {};
    }

    root.VOA.DataTableManagePropertiesSearchSort = function (){

        function getURLParameter(name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || '';
        }

        var messages = VOA.messages.en,
            $table  = $('#dataTableManagePropertiesSearchSort');

        VOA.helper.dataTableSettings($table);

        $('#agent').val(getURLParameter('agent'));

        var dataTable = $table.DataTable({
            searching: true,
            ordering: true,
            orderCellsTop: true,
            ajax: {
                data: function() {
                    var info = $table.DataTable().page.info();
                    var queryParameters = '';
                    queryParameters += '&baref=' + $('#baref').val();
                    queryParameters += '&agent=' + ($('#agent').val() || getURLParameter('agent'));
                    queryParameters += '&address=' + $('#address').val();
                    queryParameters += '&status=' + $('#status').val();

                    $table.DataTable().ajax.url('/business-rates-property-linking/properties-search-sort/json?page=' + (info.page + 1) + '&pageSize='+ info.length +'&requestTotalRowCount=true' + queryParameters);
                },
                dataSrc: 'authorisations',
                dataFilter: function(data) {
                    var json = jQuery.parseJSON(data);
                    json.recordsTotal = json.total;
                    json.recordsFiltered = json.filterTotal;
                    return JSON.stringify(json);
                }
            },
            columns: [
                {data: 'address', name: 'address'},
                {data: 'localAuthorityRef', defaultContent:'-', name: 'baref'},
                {data: null, defaultContent: '<ul class="list"><li></li><li></li></ul>', name: 'status'},
                {data: 'agents[, ].organisationName', name: 'agent'},
                {data: null, defaultContent: '<ul class="list"><li></li><li></li></ul>', 'bSortable': false}
            ],
            fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
                $('td:eq(2) ul li:eq(0)', nRow).text( messages.labels['status' + (aData.status.split('_').join('').toLowerCase())] );
                if(aData.status.toLowerCase() === 'pending'){
                    $('td:eq(2) ul li:eq(1)', nRow).html('<span class="submission-id">' + messages.labels.submissionId+ ': ' + aData.submissionId + '</span>' );
                }
                if(!aData.agents){
                    $('td:eq(3)', nRow).text('');
                }
                $('td:eq(4) ul li:eq(0)', nRow).html('<a href="/business-rates-property-linking/appoint-agent/' + aData.authorisationId + '">'+ messages.labels.appointAgent + '</a>');
                if (aData.status.toLowerCase() === 'approved' || aData.status.toLowerCase() === 'pending') {
                    $('td:eq(4) ul li:eq(1)', nRow).html('<a href="/business-rates-property-linking/property-link/' + aData.authorisationId + '/assessments' + '">' + messages.labels.viewValuations + '</a>');
                } else {
                    $('td:eq(4) ul li:eq(1)', nRow).html('<a href="/business-rates-property-linking/summary/' + aData.uarn + '">' + messages.labels.viewValuations + '</a>');
                }
            },
            fnServerParams: function(data) {
                data['order'].forEach(function(items, index) {
                    data.sortfield = data['columns'][items.column]['name'];
                    data.sortorder = data['order'][index].dir;
                });
            }
        });

        $( '#dataTableManagePropertiesSearchSort th button').on( 'click', function () {
            dataTable.draw();
        } );

        $( 'th .clear').on( 'click', function () {
            $('#dataTableManagePropertiesSearchSort th').find('input:text').val('');
            dataTable.draw();
        } );



    };

}).call(this);
