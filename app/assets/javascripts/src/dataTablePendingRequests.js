(function () {

    'use strict';

    var root = this, $ = root.jQuery;

    if (typeof VOA === 'undefined') {
        root.VOA = {};
    }

    root.VOA.DataTablePendingRequests = function (){

        //var messages = VOA.messages.en,
        // $table  = $('#dataTablePendingRequests'),
        // results;
        //
        // VOA.helper.dataTableSettings($table);
        //
        // $table.DataTable({
        //     ajax: {
        //         data: function() {
        //             var info = $table.DataTable().page.info();
        //             $table.DataTable().ajax.url('/business-rates-property-linking/manage-clients/pending-requests/json?page=' + (info.page + 1) + '&pageSize='+ info.length +'&requestTotalRowCount=true');
        //         },
        //         dataSrc: 'propertyRepresentations',
        //         dataFilter: function(data) {
        //             var json = jQuery.parseJSON(data);
        //             json.recordsTotal = json.resultCount;
        //             json.recordsFiltered = json.resultCount;
        //             results = json.totalPendingRequests;
        //             return JSON.stringify(json);
        //         }
        //     },
        //     columns: [
        //         {data: null, defaultContent: 'TICK'},
        //         {data: 'organisationName'},
        //         {data: 'address'},
        //         {data: null, defaultContent: '<ul class="list"><li></li><li></li></ul>'},
        //         {data: null},
        //         {data: null, defaultContent: '<ul class="list"><li></li><li></li></ul>'}
        //     ],
        //     fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
        //         $('td:eq(3) ul li:eq(0)', nRow).html( messages.labels.check + ': ' + messages.labels['status' + aData.checkPermission]);
        //         $('td:eq(3) ul li:eq(1)', nRow).html( messages.labels.challenge + ': ' + messages.labels['status' + aData.challengePermission]);
        //         $('td:eq(4)', nRow).text(moment(aData.createDatetime).format('LL'));
        //         $('td:eq(5) ul li:eq(0)', nRow).html('<a href="/business-rates-property-linking/representation-request/accept/' + aData.submissionId + '/' + results +'">' + messages.labels.accept + '</a>');
        //         $('td:eq(5) ul li:eq(1)', nRow).html('<a href="/business-rates-property-linking/representation-request/reject/' + aData.submissionId + '/' + results +'">' + messages.labels.reject + '</a>');
        //     }
        // });

    };


    $('#par-select-all-top, #par-select-all-bottom').click(function(event) {
        var allselected = true;
        $('input[type="checkbox"]').each(function () {
            allselected = allselected && $(this).is(':checked');
        });

        $('input[type="checkbox"]').each(function () {
            $(this).prop('checked', !allselected);
        });
        return false;
    });



}).call(this);

