var ks = {};
ks.params = {};

ks.escapeHtml = function escapeHtml(html) {
    if (html == undefined) return;
    return html.replace(/<script.*>(.*)<\/script.*>/gi, '');
};

ks.stripHtml = function stripHtml(html) {
    if (html == undefined) return;
    html = ks.escapeHtml(html);
    html = html.replace(/<br\s*[\/]?>/gi, ' ');
    html = html.replace(/<([A-Z][A-Z0-9]*)\b[^>]*>/gi, '');
    html = html.replace(/<\/(.*?)>/gi, '$#');
    html = html.replace(/(\$#\s*|\s*\$#)/gi, '$#');
    html = html.replace(/(\$#)+/gi, '$#');
    var div = document.createElement("div");
    div.innerHTML = html;
    var text = div.textContent || div.innerText || "";
    text = text.replace(/\$#/gi, ' ');
    return text;
}

ks.getBytesWithUnit = function( bytes, precision ){
    if( isNaN( bytes ) ){ return; }
    if( isNaN( precision ) ) { precision = 2; }
    var units = [ ' bytes', ' KB', ' MB', ' GB', ' TB', ' PB', ' EB', ' ZB', ' YB' ];
    var amountOf2s = Math.floor( Math.log( +bytes )/Math.log(2) );
    if( amountOf2s < 1 ){
        amountOf2s = 0;
    }
    var i = Math.floor( amountOf2s / 10 );
    bytes = +bytes / Math.pow( 2, 10*i );

    // Rounds to 3 decimals places.
    if( bytes.toString().length > bytes.toFixed(precision).toString().length ){
        bytes = bytes.toFixed(precision);
    }
    return bytes + units[i];
};

ks.formatDate = function formatDate(milliseconds) {
    if (milliseconds != undefined && milliseconds >= 0) {
        var date = new Date(milliseconds);
        return ("0" + (date.getUTCMonth()+1)).slice(-2) + "/" + ("0" + date.getUTCDate()).slice(-2) + "/" + date.getFullYear();
    }
}

ks.formatDateTimeToUTC = function formatDateTimeToUTC(milliseconds) {
    if (milliseconds != undefined && milliseconds >= 0) {
        var date = new Date(milliseconds);
        return ("0" + (date.getUTCMonth()+1)).slice(-2) + "/" + ("0" + date.getUTCDate()).slice(-2) + "/" + date.getFullYear() + " " + ("0" + date.getUTCHours()).slice(-2) + ":" + ("0" + date.getUTCMinutes()).slice(-2);
    }
};

ks.formatDateTime = function formatDateTime(milliseconds) {
    if (milliseconds != undefined && milliseconds >= 0) {
        var date = new Date(milliseconds);
        return ("0" + (date.getMonth()+1)).slice(-2) + "/" + ("0" + date.getDate()).slice(-2) + "/" + date.getFullYear() + " " + ("0" + date.getHours()).slice(-2) + ":" + ("0" + date.getMinutes()).slice(-2);
    }
}

ks.wysihtml5 = {};
ks.wysihtml5.customTemplates = {
    emphasis : function(locale) {
        return "<li>" +
            "<div class='btn-group'>" +
            "<a class='btn' data-wysihtml5-command='bold' title='CTRL+B'>B</a>" +
            "<a class='btn' data-wysihtml5-command='italic' title='CTRL+I'>I</a>" +
            "<a class='btn' data-wysihtml5-command='underline' title='CTRL+U'>U</a>" +
            "</div>" +
            "</li>";
    }
}

$('a[rel*=external]').click(function(){
    window.open($(this).attr('href'));
    return false;
});

(function($) {
    $.extend($.fn, {
        nl2br : function(is_xhtml) {
            return this.each(function() {
                var breakTag = (is_xhtml || typeof is_xhtml === 'undefined') ? '<br />' : '<br>';
                var that = $(this);
                that.html($(this).html().replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1' + breakTag + '$2'));
            });
        },
        striphtml : function() {
            return this.each(function() {
                var html = this.textContent || this.innerText || "";
                $(this).text(ks.stripHtml(html));
            });
        },
        addHtml : function(html) {
            return this.each(function() {
                if (html == undefined) { return; }

                html = ks.escapeHtml(html);
                $(this).html(html);
            });
        },
        escapeHtml : function() {
            return this.each(function() {
                var html = $(this).html() || "";
                html = ks.escapeHtml(html);
                $(this).html(html);
            });
        }
    });
})(jQuery);

(function($){
    $.fn.tabState = function(defaultId) {
        $('a[data-toggle="tab"]').on('shown', function(e){
            //save the latest tab using a cookie:
            $.cookie('last_tab', $(e.target).attr('href'));
        });

        var lastTab = $.cookie('last_tab');
        lastTab = (lastTab && ($(lastTab).closest('.tab-content').length > 0 ) ? lastTab : defaultId);
        if (lastTab) {
            $('a[href="' + lastTab + '"]').tab('show');
//            var $tab = $('a[href='+ lastTab +']').parents('li.tab');
//            if ($tab != undefined) {
//                alert($tab.html());
//                $('ul.nav-tabs').children().removeClass('active');
//                $tab.addClass('active');
//                $('div.tab-content').children().removeClass('active');
//                $(lastTab).addClass('active');
//            }
        }
    };
})(jQuery);

$("a.contacts").on("click", function (e) {
    e.preventDefault();
    $.get(ks.contactsURL, function(data) {
        $('.generated-contact').remove();
        data.forEach(function (contacts) {
            for (contact in contacts.contacts) {
                console.log(contacts.contacts[contact].name);
                var mailTo = '<a class="generated-contact" id="knappsackSupport" href="mailto:' + contacts.contacts[contact].email + '" rel="external">' + contacts.contacts[contact].name + ' (' + contacts.contacts[contact].email + ')</a>';
                $("<div class='generated-contact'>" + mailTo + "</div>").prependTo("#contactsModalDiv");
            }
            $("<p class='generated-contact' style='padding-top:.5em;'><b>" + contacts.domainName + " Administrators</b></p>").prependTo("#contactsModalDiv");
        });
        $('#contactsModal').modal({
            show: true
        });
    });
});

//Organization Context Menu
$(document).on('click', 'a.ks-orgContextMenu', function() {
    var $filter = $(this).closest('.filter');
    var map = {};
    var orgs = [];
    var organizationsUrl = ks.contextPath + '/activeOrganizations';
    $('.dropdown-menu ul.elements', $filter).empty();

    $.getJSON(organizationsUrl, function(organizations) {
        $('.dropdown-menu input.typeahead', $filter).typeahead({
            minLength: 0,
            items: organizations.length,
            source: function(query, process) {
                //reset these containers
                map = {};
                orgs = [];
                $(organizations).each(function(index, value) {
                    map[value.name] = value;
                    orgs.push(value.name);
                });
                process(orgs);
            },
            updater: function(item) {
                var orgId = map[item].id;
                window.location = ks.contextPath + '/activeOrganization/' + orgId;
                return item;
            },
            menu: $('.dropdown-menu ul.elements', $filter),
            item:'<li><a href="#"></a></li>'
        });
        $('.dropdown-menu input.typeahead', $filter).off('focus');

        //Temp fix for bootstrap typeahead 2.2.2 (Fixed in 2.3.0)
        $($filter).on('mousedown', '.dropdown-menu ul.elements', function(e) {
            e.preventDefault();
        });
        $('.dropdown-menu input.typeahead', $filter).on('focus', $('.dropdown-menu input.typeahead', $filter).typeahead.bind($('.dropdown-menu input.typeahead', $filter), 'lookup'));
        $('.dropdown-menu input.typeahead', $filter).focus();
    });
});

//Login Page
$(document).on('knappsack.login-page', function(event, params) {
    var activeTab = params.activeTab;

    if (activeTab == 'register') {
        $('#registrationTab a').tab('show');
    } else if (activeTab == 'forgotPassword') {
        $('#forgotPasswordTab a').tab('show');
    } else {
        $('#loginTab a').tab('show')
    }

    $('#openid_btns a').click(function(e) {
        e.preventDefault();

        var providerUrl = $(this).attr('data-provider');
        $('#openid_identifier').val(providerUrl);

        $('#openidForm').submit();
    });

    $('#openIdRememberMeCheckBox').hide();

    $('#forgotPasswordForm').submit(function(e) {
        e.preventDefault();

        var data = { email : $('#forgotPasswordEmail').val()};
        $.post(params.forgotPasswordURL, data);

        $(this).tab('show');

    });

    $('a[data-toggle="tab"]').on('shown', function (e) {
        var $target = $(e.target);
        var selector = $target.attr('data-target');

        if (!selector) {
            selector = $target.attr('href')
            selector = selector && selector.replace(/.*(?=#[^\s]*$)/, '') //strip for ie7
        }

        $('#tabs li.active').removeClass('active');

        if (selector == '#2') {
            $('#tabs li a[href="#2"]').parent().addClass('active');
        } else {
            $('#tabs li a[href="#1"]').parent().addClass('active');
        }

    });
});

//Activate Page
$(document).on('knappsack.activate-page', function(event, params) {
    $('#activationForm').submit(function() {
        $(this).attr('action', ks.contextPath + '/activate/' + $('#accessCode').val());
        return true;
    });

    $('#resend').click(function(e) {
        e.preventDefault();

        showConfirmationModal(params.resendCodeModalTitle);
        $('#confirmationModalSubmit').click(function () {
            ajaxReSendActivationCode();
        });
    });

    function ajaxReSendActivationCode() {
        $.get(params.resendCodeURL, function(data) {
            if (data.result) {
                $('#confirmationModal .modal-body .alert-error').hide();
                $('#confirmationModal .modal-body .alert-success').show();
                hideModal(500);
            } else {
                $('#confirmationModal .modal-body .alert-error').show();
                $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                $('#confirmationModal .close').removeAttr('disabled');
            }
        });
    }

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }

    function showConfirmationModal(title) {
        $('#confirmationModal .modal-header h3').text(title);
        $('#confirmationModal').modal();
    }

    $('#confirmationModal').on('show', function () {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    });

    $('#confirmationModal').on('hide', function () {
        $('#confirmationModalSubmit').unbind('click');
    });
});

//Categories Page
$(document).on('knappsack.categories-page', function() {
    $('.category').click(function() {
        window.location.href = ks.contextPath + '/categories/' + $(this).attr('data-id');;
    });
});

//Category Results Page
$(document).on('knappsack.category_results-page', function(event, params) {
    $('.striphtml').striphtml();

    var $applicationsTable = $('table').dataTable( {
        "sDom": "<'table-inline'<'row-fluid'<'span6'l><'pull-right'f>r>t<'row-fluid'<'span6'i><'pull-right'p>>",
        "sPaginationType": "bootstrap",
        "iDisplayLength": 5,
        "oLanguage": {
            "sLengthMenu": '<select>'+
                '<option value="5">5</option>'+
                '<option value="10">10</option>'+
                '<option value="25">25</option>'+
                '<option value="50">50</option>'+
                '<option value="-1">All</option>'+
                '</select> ' + ks.recordsPerPageText
        },
        "aoColumns": [
            { "bSortable": false, "bSearchable": false, "sWidth": "100%"},
            { "bSortable": false, "bSearchable": true, "bVisible": false, "sWidth": "0%" },
            { "bSortable": false, "bSearchable": true, "bVisible": false, "sWidth": "0%" }
        ],
        "bAutoWidth": false
    });

    $('table').show();

    $applicationsTable.$('tr').on('click', function() {
        window.location.href = ks.contextPath + '/detail/' + $(this).attr('data-id');
    });

    $('.dataTables_filter input').attr("placeholder", params.tablePlaceholderText);
});

//Change Password Page
$(document).on('knappsack.change_password-page', function(event, params) {
    $('#resend').click(function(e) {
        e.preventDefault();
        showConfirmationModal(params.resetPasswordModalTitle);
        $('#confirmationModalSubmit').click(function () {
            ajaxSubmit();
        });
    });

    function ajaxSubmit() {
        $.post(ks.contextPath + '/profile/resetPassword', function(data) {
            if (data.result) {
                $('#confirmationModal .modal-body .alert-error').hide();
                $('#confirmationModal .modal-body .alert-success').show();
                hideModal(500);
            } else {
                $('#confirmationModal .modal-body .alert-error').show();
                $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                $('#confirmationModal .close').removeAttr('disabled');
            }
        });
    }

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }

    function showConfirmationModal(title) {
        $('#confirmationModal .modal-header h3').text(title);
        $('#confirmationModal').modal();
    }

    $('#confirmationModal').on('show', function () {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    });

    $('#confirmationModal').on('hide', function () {
        $('#confirmationModalSubmit').unbind('click');
    });
});

//Detail Page
$(document).on('knappsack.detail-page', function(event, params) {
    $('.nl2br').nl2br();
    $('.html').escapeHtml();

    $('.html').each(function(index, value) {
        var html = $(this).data('html');
        if (html) {
            $(this).addHtml(html);
        }
    });

    $('.html').escapeHtml();

    var recentChanges = '';
    if (params.versions.length > 0) {
        recentChanges = params.versions[0].recentChanges;
    }
    $('#recentChanges').addHtml(recentChanges);

    var isSubscribed = $('#subscriptionBtn').attr('data-subscribed');

    if (isSubscribed == 'false') {
        var elem = $('#subscriptionBtn');
        toggleSubsribeButton(elem);
    } else {
        var elem = $('#subscriptionBtn');
        toggleUnsubscribeButton(elem);
    }

    function setPopoverTitle(elem, title) {
        $(elem).attr("data-original-title", title);
    }

    function setPopoverContent(elem, content) {
        $(elem).attr("data-content", content);
    }

    $('#subscriptionBtn').popover( {trigger: 'hover', placement: 'left'} );

    $('#subscriptionBtn').click(function(e) {
        e.preventDefault();
        $(this).popover('hide');

        var btn = $(this);

        $(btn).text('processing');

        var url;
        if (isSubscribed == 'false') {
            url = ks.contextPath + '/profile/subscribe/' + params.selectedApplication.id;
        } else {
            url = ks.contextPath + '/profile/unsubscribe/' + params.selectedApplication.id;
        }

        $.get(url, function(data) {
            if (data.result) {
                if ($(btn).attr('data-subscribed') == 'true') {
                    toggleSubsribeButton(btn);
                } else {
                    toggleUnsubscribeButton(btn);
                }
            } else {
                $(btn).button('complete');
            }
        });

        setTimeout(function(){}, 2000);

        return false;

    });

    var selectedVersionId = params.initialVersionId;

    var carouselStarted = false;
    $(params.selectedApplication.screenshots).each(function(index, value) {
        var img = $("<img />").addClass('thumbnail').attr('src', value.url)
            .load(function() {
                if (this.complete && typeof this.naturalWidth != "undefined" && this.naturalWidth != 0) {
                    var $item = $('<div>').addClass('item');
                    if (index === 0) $item.addClass('active');
                    $('#myCarousel > .carousel-inner').append($($item).append(img));
                    if (!carouselStarted) {
                        $('.carousel').carousel('cycle');
                    }
                }
            });
    });

    $('.versionSelect').click(function(e) {
        e.preventDefault();

        var version = $(this).attr('data-version');
        var recentChanges = $(this).attr('data-recent-changes');
        selectedVersionId = $(this).attr('data-id');

        $('#recentChanges').addHtml(recentChanges);

        var downloadBtnTxt = params.downloadBtnTxt;
        if (version != null && version != '') {
            downloadBtnTxt = downloadBtnTxt + version;
        }

        $('#downloadBtnTxt').text(downloadBtnTxt);

    });

    $('#downloadBtn').click(function() {
        window.location.href = ks.contextPath + '/downloadApplication/' + selectedVersionId;
        toggleUnsubscribeButton($('#subscriptionBtn'));
    });

    function toggleSubsribeButton(btn) {
        $(btn).text(params.subscribeText);
        $(btn).attr('data-subscribed', 'false');
        setPopoverTitle($(btn), params.subscribePopoverTitle);
        setPopoverContent($(btn), params.subscribePopoverContent);

        isSubscribed = 'false';
    }

    function toggleUnsubscribeButton(btn) {
        $(btn).text(params.unsubscribeText);
        $(btn).attr('data-subscribed', 'true');
        setPopoverTitle(elem, params.unsubscribePopoverTitle);
        setPopoverContent(elem, params.unsubscribePopoverContent);

        isSubscribed = 'true';
    }

    $('a.delete-application').on('click', function(e) {
        e.preventDefault();
        deleteApplicationEvent(e);
    });

    var deleteApplicationEvent = function(e) {
        e.preventDefault();
        var title = params.deleteApplicationModalTitle + ' <b>' + params.selectedApplication.name + '</b>';
        var body = $('<p>').text(params.deleteApplicationModalBody);
        var url = ks.contextPath + '/manager/deleteApplication/' + params.selectedApplication.id;
        var data = { 'title' : title, 'body' : body, 'url' : url };
        showModal("#confirmationModal", data, fnOnDeleteApplicationConfirmationModalShow, fnOnDeleteApplicationConfirmationModalHide);

        return false;
    };

    var fnOnDeleteApplicationConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').html(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            window.location.href = data.url;
        });
    };

    var fnOnDeleteApplicationConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    function showModal(id, data, fnOnShow, fnOnHide) {
        if (id.substr(0, 1) !== '#') {
            id = '#' + id;
        }

        $(id).off('show');
        $(id).on('show', fnOnShow(data));

        $(id).off('hidden');
        $(id).on('hidden', fnOnHide());

        $(id).modal();
    }
});

//Domain Access Page
$(document).on('knappsack.domain_access-page', function() {
    $('#accessForm').submit(function() {
        $(this).attr('action', ks.contextPath + '/domain/requestAccess/' + $('#accessCode').val());
        return true;
    })
});

//Home Page
$(document).on('knappsack.home-page', function(event, params) {

    $(window).load(function() {
        $('img.delay-image').each(function(index, value) {
            var $elem = $(this);
            $elem.attr('src', $elem.data('img-url'));
        });
        if ($('#applicationsTable').length > 0) {
            $applicationsTable.fnLengthChange(5);
            $('#applicationsTable').show();
        }

    });

    $('.striphtml').striphtml();

    var $applicationsTable;
    if ($('#applicationsTable').length > 0) {
        $applicationsTable = $('#applicationsTable').dataTable( {
            "sDom": "<'table-inline'<'row-fluid'<'span6'l><'pull-right'f>r>t<'row-fluid'<'span6'i><'pull-right'p>>>",
            "sPaginationType": "bootstrap",
            "iDisplayLength": -1,
            "oLanguage": {
                "sLengthMenu": '<select>'+
                    '<option value="5">5</option>'+
                    '<option value="10">10</option>'+
                    '<option value="25">25</option>'+
                    '<option value="50">50</option>'+
                    '<option value="-1">All</option>'+
                    '</select> ' + ks.recordsPerPageText
            },
            "aoColumns": [
                { "bSortable": false, "bSearchable": false, "sWidth": "100%", "sDefaultContent": "" },
                { "bSortable": false, "bSearchable": true, "bVisible": false, "sWidth": "0%", "sDefaultContent": "" },
                { "bSortable": false, "bSearchable": true, "bVisible": false, "sWidth": "0%", "sDefaultContent": "" }
            ],
            "bAutoWidth": false
            ,
            "fnCreatedRow": function(nRow, aData, iDisplayIndex) {
                $(nRow).find('.dropdown-toggle').dropdown();
                $(nRow).on('click', '.dropdown-menu', function(e) {
                    e.stopPropagation();
                });
                $(nRow).on('click', function() {
                    var url = ks.contextPath + '/detail/' + $(this).attr('data-id');
                    window.location.href = url;
                });
            }
        });
    }

    $('.dataTables_filter input').attr("placeholder", params.tablePlaceholderText);

    $('a.delete-application').on('click', function(e) {
        e.preventDefault();
        deleteApplicationEvent(e, $(this).attr('href'));
    });

    deleteApplicationEvent = function(e, url) {
        e.preventDefault();
        var title = params.deleteApplicationModalTitle;
        var body = $('<p>').text(params.deleteApplicationModalBody);
        var data = { 'title' : title, 'body' : body, 'url' : url };
        showModal("#confirmationModal", data, fnOnDeleteApplicationConfirmationModalShow, fnOnDeleteApplicationConfirmationModalHide);

        return false;
    };

    var fnOnDeleteApplicationConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            window.location.href = data.url;
        });
    };

    var fnOnDeleteApplicationConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    function showModal(id, data, fnOnShow, fnOnHide) {
        if (id.substr(0, 1) !== '#') {
            id = '#' + id;
        }

        $(id).off('show');
        $(id).on('show', fnOnShow(data));

        $(id).off('hidden');
        $(id).on('hidden', fnOnHide());

        $(id).modal();
    }
});

//Manager Checklist Fragment
$(document).on('knappsack.manager_checklist-fragment', function() {
    $('#managerChecklistDiv').hide();
    $.ajax({url:ks.contextPath + '/manager/checklist', success:managerChecklist});

    function managerChecklist(managerChecklist) {
        if (managerChecklist.notComplete && managerChecklist.organizationId != null) {
            $('#managerChecklistDiv').show();
            if (!managerChecklist.groupNeeded) {
                $('#createGroupDiv').hide();
                $('#completedGroupDiv').show();
            }
            var createGroupLink = ks.contextPath + managerChecklist.createGroupURL;
            $('#createGroupLink').attr('href', createGroupLink);
            if (!managerChecklist.appNeeded) {
                $('#createApplicationDiv').hide();
            }
            if (managerChecklist.hasApps) {
                $('#completedApplicationDiv').show();
            }
            var createApplicationLink = ks.contextPath + managerChecklist.createApplicationURL;
            $('#createApplicationLink').attr('href', createApplicationLink);
            if (!managerChecklist.appVersionNeeded) {
                $('#createAppVersionDiv').hide();
            }
            if (managerChecklist.hasAppVersions) {
                $('#completedAppVersionDiv').show();
            }
            var createAppVersionLink = ks.contextPath + managerChecklist.createApplicationVersionURL;
            $('#createAppVersionLink').attr('href', createAppVersionLink);
            if (!managerChecklist.organizationUserNeeded) {
                $('#inviteOrganizationUserDiv').hide();
                $('#completedInviteOrganizationUserDiv').show();
            }
            var inviteOrganizationUserLink = ks.contextPath + managerChecklist.inviteOrganizationUsersURL;
            $('#inviteOrganizationUserLink').attr('href', inviteOrganizationUserLink);
            $('#managerChecklistSuccessProgress').width(managerChecklist.percentComplete + "%");
            $('#managerChecklistIncompleteProgress').width((100 - managerChecklist.percentComplete) + "%");
        }
    }
});

//System Notifications
$(document).on('knappsack.system_notifications-fragment', function(event, params) {
    $.ajax( {
        url: ks.contextPath + '/getSystemNotifications',
        data: {"types": params.systemNotificationTypes}
    }).done(function(result) {
            if (result) {
                $.each(result, function(index, value) {

                    $('.system_notifications .content').append(
                        $('<div>').addClass('alert alert-notify')
                            .append($('<button>').addClass('close').attr('type', 'button').attr('data-dismiss', 'alert').text('x'))
                            .append($('<i>').addClass('icon-exclamation-sign'))
                            .append($('<strong>').text(' System Notification: '))
                            .append($('<span>').text(ks.escapeHtml(value.message)))
                    );
//                    var type = 'alert';
//                    switch (value.notificationSeverity) {
//                        case 'INFO':
//                            type = 'information';
//                            break;
//                        case 'WARNING':
//                            type = 'warning';
//                            break;
//                        case 'ERROR':
//                            type = 'error';
//                            break;
//                    }

//                    systemNotifications.push($('.system-notifications').noty({
//                        text: escapeHtml(value.message),
//                        type: type,
//                        layout: 'topCenter',
//                        closeWith: ['click']
//                    }));
                });
            }
        });
});

//Manage Application
$(document).on('knappsack.manage_application-page', function(event, params) {
    $(this).tabState('#application');

    $('a[data-toggle=tooltip]').tooltip();

    var tempUrl, selectedElement;
    var successFunction;
    var downloadSummaryTable;
    var downloadDetailsTable;

    $("#provisioningFileDiv").hide();
    $("#propertiesListDiv").hide();
    $("#downloadSummaryTableDiv").hide();
    $("#downloadDetailsTableDiv").hide();
    $("#viewDownloadSummary").hide();
    $("#viewDownloadDetails").hide();

    $('#description').wysihtml5({
        customTemplates: ks.wysihtml5.customTemplates,
        "font-styles": false, //Font styling, e.g. h1, h2, etc. Default true
        "image": false, //Button to insert an image. Default true,
        "color": false, //Button to change color of font
        "stylesheets": false
    });

    $('#version-notes').wysihtml5({
        customTemplates: ks.wysihtml5.customTemplates,
        "font-styles": false, //Font styling, e.g. h1, h2, etc. Default true
        "image": false, //Button to insert an image. Default true,
        "color": false, //Button to change color of font
        "stylesheets": false

    });

    $('#applicationVersionsTable').dataTable( {
        "sDom": "<'table-inline'<<'span6'l><'pull-right'f>r>t<<'span6'i><'pull-right'p>>>",
        "sPaginationType": "bootstrap",
        "oLanguage": {
            "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
        },
        "aoColumns": [
            null,
            null,
            { "bSortable": false },
            { "bSortable": false }
        ]
    });

    if (params.editing) {;
        if (params.hasIcon) {
            $('#iconDiv a[data-dismiss=fileupload]').on('click', function(e) {
                e.preventDefault();
                e.stopImmediatePropagation();

                var body = $('<p>').text(params.deleteIconModalBody);
                var successMessage = $('<h2>').text(params.deleteIconModalSuccess);
                var errorMessage = $('<div>').append($('<h2>').text(params.deleteIconModalError));
                var data = { 'title' : params.deleteIconModalTitle, 'body' : body, 'successMessage' : successMessage, 'errorMessage' : errorMessage };
                showModal("#confirmationModal", data, fnOnDeleteIconConfirmationModalShow, fnOnDeleteIconConfirmationModalHide);
            });
        }
        $("#viewDownloadSummary").show();
        $("#viewDownloadDetails").show();
    }

    $("#viewDownloadSummary").live("click", function (e) {
        e.preventDefault();
        tempUrl = $(this).val();
        $("#downloadSummaryTableDiv").show();
        $("#downloadDetailsTableDiv").hide();

        if (downloadSummaryTable !== undefined) {
            downloadSummaryTable.fnClearTable(false);
            downloadSummaryTable.fnReloadAjax(tempUrl);
        } else {
            downloadSummaryTable = $('#downloadSummaryTable').dataTable({
                "bProcessing": true,
                "sAjaxDataProp": "",
                "sAjaxSource": tempUrl,
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
                },
                "aoColumns": [
                    {
                        "aTargets": [0],
                        "mData": "applicationName",
                        "sDefaultContent": "N/A"
                    },
                    {
                        "aTargets": [1],
                        "mData": "applicationVersionName",
                        "sDefaultContent": "N/A"
                    },
                    {
                        "aTargets": [2],
                        "mData": "total",
                        "sDefaultContent": "N/A"
                    }
                ]
            });
        }
    });

    $("#viewDownloadDetails").live("click", function (e) {
        e.preventDefault();
        tempUrl = $(this).val();
        $("#downloadSummaryTableDiv").hide();
        $("#downloadDetailsTableDiv").show();

        if (downloadDetailsTable !== undefined) {
            downloadDetailsTable.fnClearTable(false);
            downloadDetailsTable.fnReloadAjax(tempUrl);
        } else {
            downloadDetailsTable = $('#downloadDetailsTable').dataTable({
                "sDom": "<'row-fluid'<'pull-right'T>><'row-fluid'r><'row-fluid'<'span6'l><'span6 pull-right'f>>t<'row-fluid'<'span6'i><'pull-right'p>>",
                "oTableTools":{
                    "sSwfPath": params.swfPath,
                    "aButtons": [
                        "copy",
                        "print",
                        {
                            "sExtends": "collection",
                            "sButtonText": 'Save <span class="caret"/>',
                            "aButtons": ["csv", "xls", "pdf"]
                        }
                    ]
                },
                "bProcessing": true,
                "sAjaxDataProp": "",
                "sAjaxSource": tempUrl,
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
                },
                "aoColumns": [
                    {
                        "aTargets": [0],
                        "mData": "applicationName",
                        "sDefaultContent": "N/A"
                    },
                    {
                        "aTargets": [1],
                        "mData": "applicationVersionName",
                        "sDefaultContent": "N/A"
                    },
                    {
                        "aTargets": [2],
                        "mData": "userName",
                        "sDefaultContent": "N/A"
                    },
                    {
                        "aTargets": [3],
                        "mData": "userEmail",
                        "sDefaultContent": "N/A"
                    },
                    {
                        "aTargets": [4],
                        "mData": "userAgent",
                        "sDefaultContent": "N/A"
                    },
                    {
                        "aTargets": [5],
                        "mData": "remoteAddress",
                        "sDefaultContent": "N/A"
                    },
                    {
                        "aTargets": [6],
                        "mData": "date",
                        "sDefaultContent": "N/A"
                    }
                ]
            });
        }
    });

    $('#btnAdd').click(function () {
        var numScreenshots = $('#screenshotsDiv').find('.fileupload').size();

        var clone = $('#screenshotsDiv .cloneable:first').clone();
        if (clone) {
            $(clone).find('input[type=hidden]').remove();
            $(clone).find('div.fileupload-preview > img').remove();
            $(clone).find('input').attr('name', 'screenshots[' + numScreenshots + ']');
            $(clone).fileupload('clear');
            $(clone).appendTo('#screenshotsDiv > div.controls');
        }

        return false;

    });

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }

    $('#confirmationModal').on('show', function () {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    });

    $('#confirmationModal').on('hide', function () {
        tempUrl = '';
        selectedElement = '';
        successFunction = '';
    });

    $('#applicationType').on('change', function(e) {
        checkKeyVaultEntries();
    });

    $('#groupList').on('change', function(e) {
        checkKeyVaultEntries();
    });

    checkKeyVaultEntries();

    function checkKeyVaultEntries() {
        $('#keyVaultEntries').children('option:not(:first)').remove();
        if ($('#applicationType').val() && $('#groupList').val()) {
            $.get(ks.contextPath + '/manager/getAllKeyVaultEntriesForDomain', {'domainId' : $('#groupList').val(), 'applicationType' : $('#applicationType').val()}, function(result) {
                if (result) {
                    $(result).each(function(index, value) {
                        $('#keyVaultEntries').append($('<option>').val(this.id).text(this.name));
                    });
                    hideKeyVaultEntries();
                    if (result.length) {
                        $('#resignerDiv').show();
                    } else {
                        $('#resignerDiv').hide();
                    }
                }
            });
        } else {
            $('#resignerDiv').hide();
        }
    }

    if ($('#keyVaultEntries').val()) {
        showKeyVaultEntries();
    } else {
        hideKeyVaultEntries();
    }

    function showKeyVaultEntries() {
        $('#btnGrpVisibility > .btn').each(function() {
            var $this = $(this);
            if ($this.data('value')) {
                $this.addClass("btn-info").addClass('active');
                $this.siblings().removeClass("btn-info").removeClass('active');

                $('#keyVaultEntries').attr('required', 'required');
                $('#keyVaultEntriesDiv').show();

                return;
            }
        });
    }

    function hideKeyVaultEntries() {
        $('#resignerDiv #btnGrpVisibility > .btn').each(function() {
            var $this = $(this);
            if (!$this.data('value')) {
                $this.addClass("btn-info").addClass('active');
                $this.siblings().removeClass("btn-info").removeClass('active');

                $('#keyVaultEntries').removeAttr('required');
                $('#keyVaultEntries').val('');
                $('#keyVaultEntriesDiv').hide();

                return;
            }
        });
    }

    $("[data-switch='true']").on("click", ".btn", function() {
        var $this = $(this);

        if ($this.data('value')) {
            showKeyVaultEntries();
        } else {
            hideKeyVaultEntries();
        }
    });

    $('#createNewGroupBtn').on('click', function(e) {
        e.preventDefault;
        createGroupEvent(e);
    });

    var createGroupEvent = function(e) {
        e.preventDefault();

        var grpName = $('#createNewGroup').val();
        var body = $('<p>').text(params.createGroupModalBody);
        var successMessage = $('<h2>').text(params.createGroupModalSuccess);
        var errorMessage = $('<div>').append($('<h2>').text(params.createGroupModalError)).append($('<ul>'));
        var data = { 'title' : params.createGroupModalTitle, 'body' : body, 'successMessage' : successMessage, 'errorMessage' : errorMessage, 'name' : grpName };
        showModal("#confirmationModal", data, fnOnCreateGroupConfirmationModalShow, fnOnCreateGroupConfirmationModalHide);
    };

    var fnOnCreateGroupConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            $.post(ks.contextPath + '/manager/createGroup', {'name' : data.name}, function(result) {
                $('#confirmationModal > .modal-body > .body').hide();
                if (result.result) {
                    $('#confirmationModal .modal-body .alert-error').hide();
                    if (data.successMessage) {
                        $('#confirmationModal > .modal-body > .alert-success > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .custom').append(data.successMessage).show();
                    } else {
                        $('#confirmationModal > .modal-body > .alert-success > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-success').show();
                    $('#groupList').append($('<option>').val(result.value.id).text(result.value.name)).val(result.value.id);
                    $('#createNewGroup').val('');
                    hideModal(1000);
                } else {
                    $('#confirmationModal .modal-body .alert-success').hide();
                    if (data.errorMessage) {
                        $('#confirmationModal > .modal-body > .alert-error > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .custom').append(data.errorMessage).show();
                        $('#confirmationModal > .modal-body > .alert-error > .custom ul > li').remove();
                        $(result.value).each(function(index, value) {
                            $('#confirmationModal > .modal-body > .alert-error > .custom ul').append($('<li>').text(value.value));
                        });
                    } else {
                        $('#confirmationModal > .modal-body > .alert-error > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-error').show();
                    $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                    $('#confirmationModal .close').removeAttr('disabled');
                }
            });
        });
    };

    var fnOnCreateGroupConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    var fnOnDeleteIconConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            $.post(ks.contextPath + '/manager/deleteIcon/' + $('#id').val(), function(result) {
                $('#confirmationModal > .modal-body > .body').hide();
                if (result.result) {
                    $('#confirmationModal .modal-body .alert-error').hide();
                    if (data.successMessage) {
                        $('#confirmationModal > .modal-body > .alert-success > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .custom').append(data.successMessage).show();
                    } else {
                        $('#confirmationModal > .modal-body > .alert-success > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-success').show();
                    params.hasIcon = false;
                    $('#iconDiv a[data-dismiss=fileupload]').off('click');
                    $('#iconDiv div.fileupload-preview > img').remove();
                    $('#iconDiv div[data-provides=fileupload]').removeClass('fileupload-exists').addClass('fileupload-new');
                    $('#iconDiv input[type=hidden]').remove();
                    $('#iconDiv div.fileupload').fileupload({'name':'icon'});
                    hideModal(1000);
                } else {
                    $('#confirmationModal .modal-body .alert-success').hide();
                    if (data.errorMessage) {
                        $('#confirmationModal > .modal-body > .alert-error > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .custom').append(data.errorMessage).show();
                    } else {
                        $('#confirmationModal > .modal-body > .alert-error > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-error').show();
                    $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                    $('#confirmationModal .close').removeAttr('disabled');
                }
            });
        });
    };

    var fnOnDeleteIconConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    function showModal(id, data, fnOnShow, fnOnHide) {
        if (id.substr(0, 1) !== '#') {
            id = '#' + id;
        }

        $(id).off('show');
        $(id).on('show', fnOnShow(data));

        $(id).off('hidden');
        $(id).on('hidden', fnOnHide());

        $(id).modal();
    }

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }

    $('#screenshotsDiv a[data-dismiss=fileupload]').on('click', function(e) {
        e.preventDefault();
        var index = $(this).data('index');
        if (index !== undefined) {
            e.stopImmediatePropagation();
            deleteScreenshotEvent(index);
        }
    });

    var deleteScreenshotEvent = function(index) {
        if (index !== undefined) {
            var body = $('<p>').text(params.deleteScreenshotModalBody);
            var successMessage = $('<h2>').text(params.deleteScreenshotModalSuccess);
            var errorMessage = $('<div>').append($('<h2>').text(params.deleteScreenshotModalError)).append($('<ul>'));
            var data = { 'title' : params.deleteScreenshotModalTitle, 'body' : body, 'successMessage' : successMessage, 'errorMessage' : errorMessage, 'index' : index };
            showModal("#confirmationModal", data, fnOnDeleteScreenshotConfirmationModalShow, fnOnDeleteScreenshotConfirmationModalHide);
        }
    }

    var fnOnDeleteScreenshotConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            $.post(ks.contextPath + '/manager/deleteScreenshot/' + $('#id').val() + '/' + data.index, function(result) {
                $('#confirmationModal > .modal-body > .body').hide();
                if (result.result) {
                    $('#confirmationModal .modal-body .alert-error').hide();
                    if (data.successMessage) {
                        $('#confirmationModal > .modal-body > .alert-success > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .custom').append(data.successMessage).show();
                    } else {
                        $('#confirmationModal > .modal-body > .alert-success > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-success').show();
                    var $elem = $('#screenshotsDiv a[data-dismiss=fileupload][data-index=' + data.index + ']').closest('div.fileupload');
                    $($elem).find('a[data-dismiss=fileupload][data-index=' + data.index + ']').off('click');
                    $($elem).find('div.fileupload-preview > img').remove();
                    $($elem).removeClass('fileupload-exists').addClass('fileupload-new');
                    $($elem).find('input[type=hidden]').remove();
                    $($elem).fileupload({'name':'screenshots[' + data.index + ']'});
                    hideModal(1000);
                } else {
                    $('#confirmationModal .modal-body .alert-success').hide();
                    if (data.errorMessage) {
                        $('#confirmationModal > .modal-body > .alert-error > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .custom').append(data.errorMessage).show();
                    } else {
                        $('#confirmationModal > .modal-body > .alert-error > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-error').show();
                    $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                    $('#confirmationModal .close').removeAttr('disabled');
                }
            });
        });
    };

    var fnOnDeleteScreenshotConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    var getApplicationVersionsUrl = ks.contextPath + '/api/v1/applications/' + params.parentApplicationId + '/applicationVersions';

    var $versionHistoryTable = $('#versionHistoryTable').dataTable( {
        "sDom": "<'table-inline'<<'span6'l><'pull-right'f>r>t<<'span6'i><'pull-right'p>>>",
        "sPaginationType": "bootstrap",
        "oLanguage": {
            "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
        },
        "bSort": true,
        "sAjaxSource": getApplicationVersionsUrl,
        "sAjaxDataProp":"",
        "bProcessing": true,
        "bAutoWidth": true,
        "aoColumnDefs" : [
            {
                "aTargets" : [0],
                "mData" : "versionName",
                "sDefaultContent" : "N/A"
            },
            {
                "aTargets" : [1],
                "mData" : "appState",
                "mRender" : function(data, type, full) {
                    var returnVal = data;
                    $(params.appStates).each(function(index, value) {
                        if (value.key.$name == data) {
                            returnVal = value.value;
                            return;
                        }
                    });
                    return returnVal;
                },
                "sDefaultContent" : "N/A"
            },
            {
                "bSortable" : false,
                "aTargets" : [2],
                "mData" : "id",
                "mRender" : function(data, type, full) {
                    var editBtn = $('<a>').addClass('btn edit-version').attr('href', ks.contextPath + '/manager/editVersion/' + params.parentApplicationId + '/' + data).attr('title', params.editVersion).append($('<i>').addClass('icon-edit'));
                    var deleteBtn = $('<a>').addClass('btn btn-danger delete-version').attr('href', '#').attr('title', params.deleteVersion).append($('<i>').addClass('icon-trash'));

                    return $('<div>').append(editBtn).append('&nbsp;').append(deleteBtn).html();
                },
                "sDefaultContent" : "N/A"
            }
        ],
        "fnCreatedRow": function( nRow, aData, iDataIndex ) {
            $(nRow).data('version', aData);
            $(nRow).find('a.edit-version')
        },
        "fnInitComplete": function() {
            this.fnAdjustColumnSizing(true);
        },
        "fnDrawCallback": function () {
            $('.delete-version').on('click', deleteVersionEvent);
        }
    });

    var deleteVersionEvent = function(e) {
        e.preventDefault();
        var versionData = $(this).closest('tr').data('version');
        var title = params.deleteVersionModalTitle;
        if (versionData) {
            title += ' ' + versionData.versionName;
        }
        var body = $('<p>').text(params.deleteVersionModalBody);
        var data = { 'title' : title, 'body' : body, 'versionId' : versionData.id };
        showModal("#confirmationModal", data, fnOnDeleteVersionConfirmationModalShow, fnOnDeleteVersionConfirmationModalHide);
    };

    var fnOnDeleteVersionConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            window.location.href = ks.contextPath + '/manager/deleteVersion/' + params.parentApplicationId + '/' + data.versionId;
        });
    };

    var fnOnDeleteVersionConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    $('.delete-application').on('click', function(e) {
        e.preventDefault();
        var title = params.deleteApplicationModalTitle;
        var body = $('<p>').text(params.deleteApplicationModalBody);
        var data = { 'title' : title, 'body' : body, 'applicationId' : params.parentApplicationId.id };
        showModal("#confirmationModal", data, fnOnDeleteApplicationConfirmationModalShow, fnOnDeleteVersionConfirmationModalHide);
    });

    var fnOnDeleteApplicationConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            window.location.href = ks.contextPath + '/manager/deleteApplication/' + params.parentApplicationId;
        });
    };

    $('#applicationsForm').on('submit', function(e) {
        $('input[name^="screenshots"][value=""]').remove();

        return true;
    });

});

//Manage Application Version
$(document).on('knappsack.manage_application_version-page', function(event, params) {
    //        $("#progressDiv").hide();

    $('#guestGroupIds').multiselect();
    $('a[data-toggle=tooltip]').tooltip();

    $('#recentChanges').wysihtml5({
        customTemplates: ks.wysihtml5.customTemplates,
        "font-styles": false, //Font styling, e.g. h1, h2, etc. Default true
        "image": false, //Button to insert an image. Default true,
        "color": false, //Button to change color of font
        "stylesheets": false
    });

    if(params.editing) {
        if(params.versionHasInstallFile) {
            $("#installationFileDiv").css('display', 'none');
            $('#appFile').removeAttr('required');
        }
        $('#versionName').attr('readonly', 'readonly');
    }

    if ($('#keyVaultEntries').val()) {
        showKeyVaultEntries();
    } else {
        hideKeyVaultEntries();
    }

    function showKeyVaultEntries() {
        $('#btnGrpVisibility > .btn').each(function() {
            var $this = $(this);
            if ($this.data('value')) {
                $this.addClass("btn-info").addClass('active');
                $this.siblings().removeClass("btn-info").removeClass('active');

                $('#keyVaultEntries').attr('required', 'required');
                $('#keyVaultEntriesDiv').show();

                return;
            }
        });
    }

    function hideKeyVaultEntries() {
        $('#btnGrpVisibility > .btn').each(function() {
            var $this = $(this);
            if (!$this.data('value')) {
                $this.addClass("btn-info").addClass('active');
                $this.siblings().removeClass("btn-info").removeClass('active');

                $('#keyVaultEntries').removeAttr('required');
                $('#keyVaultEntries').val('');
                $('#keyVaultEntriesDiv').hide();

                return;
            }
        });
    }

    var getApplicationVersionsUrl = ks.contextPath + '/api/v1/applications/' + params.parentApplicationId + '/applicationVersions';

    var $versionHistoryTable = $('#versionHistoryTable').dataTable( {
        "sDom": "<'table-inline'<<'span6'l><'pull-right'f>r>t<<'span6'i><'pull-right'p>>>",
        "sPaginationType": "bootstrap",
        "oLanguage": {
            "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
        },
        "bSort": true,
        "sAjaxSource": getApplicationVersionsUrl,
        "sAjaxDataProp":"",
        "bProcessing": true,
        "bAutoWidth": true,
        "aoColumnDefs" : [
            {
                "aTargets" : [0],
                "mData" : "versionName",
                "sDefaultContent" : "N/A"
            },
            {
                "aTargets" : [1],
                "mData" : "appState",
                "mRender" : function(data, type, full) {
                    var returnVal = data;
                    $(params.appStates).each(function(index, value) {
                        if (value.key.$name == data) {
                            returnVal = value.value;
                            return;
                        }
                    });
                    return returnVal;
                },
                "sDefaultContent" : "N/A"
            },
            {
                "bSortable" : false,
                "aTargets" : [2],
                "mData" : "id",
                "mRender" : function(data, type, full) {
                    var editBtn = $('<a>').addClass('btn edit-version').attr('href', ks.contextPath + '/manager/editVersion/' + params.parentApplicationId + '/' + data).attr('title', params.editVersion).append($('<i>').addClass('icon-edit'));
                    var deleteBtn = $('<a>').addClass('btn btn-danger delete-version').attr('href', '#').attr('title', params.deleteVersion).append($('<i>').addClass('icon-trash'));

                    return $('<div>').append(editBtn).append('&nbsp;').append(deleteBtn).html();
                },
                "sDefaultContent" : "N/A"
            }
        ],
        "fnCreatedRow": function( nRow, aData, iDataIndex ) {
            $(nRow).data('version', aData);
        },
        "fnInitComplete": function() {
            this.fnAdjustColumnSizing(true);
        },
        "fnDrawCallback": function () {
            $('.delete-version').on('click', deleteVersionEvent);
        }
    });

    var deleteVersionEvent = function(e) {
        e.preventDefault();
        var versionData = $(this).closest('tr').data('version');
        var title = params.deleteVersionModalTitle;
        if (versionData) {
            title += ' ' + versionData.versionName;
        }
        var body = $('<p>').text(params.deleteVersionModalBody);
        var data = { 'title' : title, 'body' : body, 'versionId' : versionData.id };
        showModal("#confirmationModal", data, fnOnDeleteVersionConfirmationModalShow, fnOnDeleteVersionConfirmationModalHide);
    };

    var fnOnDeleteVersionConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            window.location.href = ks.contextPath + '/manager/deleteVersion/' + params.parentApplicationId + '/' + data.versionId;
        });
    };

    var fnOnDeleteVersionConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    function showModal(id, data, fnOnShow, fnOnHide) {
        if (id.substr(0, 1) !== '#') {
            id = '#' + id;
        }

        $(id).off('show');
        $(id).on('show', fnOnShow(data));

        $(id).off('hidden');
        $(id).on('hidden', fnOnHide());

        $(id).modal();
    }

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }

    $("[data-switch='true']").on("click", ".btn", function() {
        var $this = $(this);

        if ($this.data('value')) {
            showKeyVaultEntries();
        } else {
            hideKeyVaultEntries();
        }
    });
});

//Invite User
$(document).on('knappsack.invite_user-page', function(event, params) {
    var singleUrl = /*[[@{/manager/sendInvitation}]]*/ '';
    var batchUrl = /*[[@{/manager/batchInvitations}]]*/ '';

    $('a[data-toggle=tooltip]').tooltip();

    $('#groupList').multiselect({
        onChange:function(element, checked) {
            if ($(element).parent('select').find('option:selected').length <= 0 && $('input.orgRole[type=radio]:checked').val() !== 'ROLE_ORG_GUEST') {
                disableGroupRoles();
            } else {
                enableGroupRoles();
            }
        }
    });

    if(params.isGroupInvite) {
        $("#invitationOrganizationRoleDiv").hide()
    }

    if (!params.isActiveOrganizationAdmin || $('input:radio[name="organizationUserRole"]:checked').val() === 'ROLE_ORG_GUEST') {
        enableGroupRoles();
    }

    function disableGroupRoles() {
        $('input.groupRole[type=radio]').each(function(index, value) {
            $(this).attr('disabled', 'disabled');
            $(this).closest('.radio').addClass('muted');
        });
    }

    function enableGroupRoles() {
        $('input.groupRole[type=radio]').each(function(index, value) {
            $(this).removeAttr('disabled');
            $(this).closest('.radio').removeClass('muted');
        });
    }

    $('input.orgRole[type=radio]').change(function(e) {
        if ($(this).val() === 'ROLE_ORG_GUEST') {
            enableGroupRoles();
        } else if ($('#groupIds option:selected').length <= 0) {
            disableGroupRoles();
        }
    });

    $('.btn-primary').bind('click',function(){
        value = $(this).attr('value');
        if(value == 'batch') {
            url = batchUrl;
        } else if(value == 'single') {
            url = singleUrl;
        }
    });

    $('#createNewGroupBtn').on('click', function(e) {
        e.preventDefault;
        createGroupEvent(e);
    });

    var createGroupEvent = function(e) {
        e.preventDefault();

        var grpName = $('#createNewGroup').val();
        var body = $('<p>').text(params.createGroupModalBody);
        var successMessage = $('<h2>').text(params.createGroupModalSuccess);
        var errorMessage = $('<div>').append($('<h2>').text(params.createGroupModalError)).append($('<ul>'));
        var data = { 'title' : params.createGroupModalTitle, 'body' : body, 'successMessage' : successMessage, 'errorMessage' : errorMessage, 'name' : grpName };
        showModal("#confirmationModal", data, fnOnCreateGroupConfirmationModalShow, fnOnCreateGroupConfirmationModalHide);
    };

    var fnOnCreateGroupConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            $.post(ks.contextPath + '/manager/createGroup', {'name' : data.name}, function(result) {
                $('#confirmationModal > .modal-body > .body').hide();
                if (result.result) {
                    $('#confirmationModal .modal-body .alert-error').hide();
                    if (data.successMessage) {
                        $('#confirmationModal > .modal-body > .alert-success > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .custom').append(data.successMessage).show();
                    } else {
                        $('#confirmationModal > .modal-body > .alert-success > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-success').show();
                    var $groupList = $('#groupList');
                    $groupList.prepend($('<option>').val(result.value.id).text(result.value.name).attr('selected', 'selected'))
                        .html($('option', $groupList).sort(function(a,b) { return a.text.toUpperCase() == b.text.toUpperCase() ? 0 : a.text.toUpperCase() < b.text.toUpperCase() ? -1 : 1; }))
                        .multiselect('rebuild')
                        .multiselect('refresh');
                    $('#createNewGroup').val('');
                    enableGroupRoles();
                    hideModal(1000);
                } else {
                    $('#confirmationModal .modal-body .alert-success').hide();
                    if (data.errorMessage) {
                        $('#confirmationModal > .modal-body > .alert-error > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .custom').append(data.errorMessage).show();
                        $('#confirmationModal > .modal-body > .alert-error > .custom ul > li').remove();
                        $(result.value).each(function(index, value) {
                            $('#confirmationModal > .modal-body > .alert-error > .custom ul').append($('<li>').text(value.value));
                        });
                    } else {
                        $('#confirmationModal > .modal-body > .alert-error > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-error').show();
                    $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                    $('#confirmationModal .close').removeAttr('disabled');
                }
            });
        });
    };

    var fnOnCreateGroupConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    function showModal(id, data, fnOnShow, fnOnHide) {
        if (id.substr(0, 1) !== '#') {
            id = '#' + id;
        }

        $(id).off('show');
        $(id).on('show', fnOnShow(data));

        $(id).off('hidden');
        $(id).on('hidden', fnOnHide());

        $(id).modal();
    }

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }
});

//Invite Batch Users
$(document).on('knappsack.invite_batch_users-page', function(event, params) {
    $('a[data-toggle=tooltip]').tooltip();

    $('#groupList').multiselect();

    if ($('#invitation-data > tbody > tr').length <= 0) {
        $('#submitBtn').attr('disabled', 'disabled');
    }


    var $dataTable = $('#invitation-data').dataTable( {
        "sDom": "<'table-inline'<<'span6'l><'pull-right'f>r>t<<'span6'i><'pull-right'p>>>",
        "sPaginationType": "bootstrap",
        "oLanguage": {
            "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
        },
        "aoColumns": [
            null,
            null,
            { "bSortable": false }
        ]
    });

    $('.delete-email').on('click', function(e) {
        e.preventDefault();
        $dataTable.fnDeleteRow($(this).closest('tr')[0]);
    });


    $('form').submit(function(e) {
        var $form = $(this);
        $(':submit').attr("disabled", "disabled");

        $($dataTable.fnGetHiddenNodes()).each(function() {
            var inputs = $('input', this);
            $(inputs).each(function() {
                var input = $(this).detach().attr('type', 'hidden');
                $(input).appendTo($form);
            });
            var select = $('select', this);
            var selectInput = $('<input>').attr('name', $(select).attr('name')).attr('type', 'hidden').val($(select).val());
            $(selectInput).appendTo($form);
        });
        return true;
    });

    $('#createNewGroupBtn').on('click', function(e) {
        e.preventDefault;
        createGroupEvent(e);
    });

    var createGroupEvent = function(e) {
        e.preventDefault();

        var grpName = $('#createNewGroup').val();
        var body = $('<p>').text(params.createGroupModalBody);
        var successMessage = $('<h2>').text(params.createGroupModalSuccess);
        var errorMessage = $('<div>').append($('<h2>').text(params.createGroupModalError)).append($('<ul>'));
        var data = { 'title' : params.createGroupModalTitle, 'body' : body, 'successMessage' : successMessage, 'errorMessage' : errorMessage, 'name' : grpName };
        showModal("#confirmationModal", data, fnOnCreateGroupConfirmationModalShow, fnOnCreateGroupConfirmationModalHide);
    };

    var fnOnCreateGroupConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            $.post(ks.contextPath + '/manager/createGroup', {'name' : data.name}, function(result) {
                $('#confirmationModal > .modal-body > .body').hide();
                if (result.result) {
                    $('#confirmationModal .modal-body .alert-error').hide();
                    if (data.successMessage) {
                        $('#confirmationModal > .modal-body > .alert-success > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .custom').append(data.successMessage).show();
                    } else {
                        $('#confirmationModal > .modal-body > .alert-success > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-success > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-success').show();
                    var $groupList = $('#groupList');
                    $groupList.prepend($('<option>').val(result.value.id).text(result.value.name).attr('selected', 'selected'))
                        .html($('option', $groupList).sort(function(a,b) { return a.text.toUpperCase() == b.text.toUpperCase() ? 0 : a.text.toUpperCase() < b.text.toUpperCase() ? -1 : 1; }))
                        .multiselect('rebuild')
                        .multiselect('refresh');
                    $('#createNewGroup').val('');
                    hideModal(1000);
                } else {
                    $('#confirmationModal .modal-body .alert-success').hide();
                    if (data.errorMessage) {
                        $('#confirmationModal > .modal-body > .alert-error > .default').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .custom').append(data.errorMessage).show();
                        $('#confirmationModal > .modal-body > .alert-error > .custom ul > li').remove();
                        $(result.value).each(function(index, value) {
                            $('#confirmationModal > .modal-body > .alert-error > .custom ul').append($('<li>').text(value.value));
                        });
                    } else {
                        $('#confirmationModal > .modal-body > .alert-error > .custom').hide();
                        $('#confirmationModal > .modal-body > .alert-error > .default').show();
                    }
                    $('#confirmationModal > .modal-body > .alert-error').show();
                    $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                    $('#confirmationModal .close').removeAttr('disabled');
                }
            });
        });
    };

    var fnOnCreateGroupConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    function showModal(id, data, fnOnShow, fnOnHide) {
        if (id.substr(0, 1) !== '#') {
            id = '#' + id;
        }

        $(id).off('show');
        $(id).on('show', fnOnShow(data));

        $(id).off('hidden');
        $(id).on('hidden', fnOnHide());

        $(id).modal();
    }

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }
});

//View Groups
$(document).on('knappsack.groups-page', function(event, params) {
    var getGroupsUrl = ks.contextPath +  '/manager/getGroupsForUser';

    var $groupTable = $('#groupTable').dataTable( {
        "sDom": "<'table-inline'<<'span6'l><'pull-right'f>r>t<<'span6'i><'pull-right'p>>>",
        "sPaginationType": "bootstrap",
        "oLanguage": {
            "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
        },
        "bSort": true,
        "sAjaxSource": getGroupsUrl,
        "sAjaxDataProp":"",
        "bProcessing": true,
        "bAutoWidth": true,
        "aoColumnDefs" : [
            {
                "aTargets" : [0],
                "mData" : "name",
                "sDefaultContent" : params.notApplicable
            },
            {
                "aTargets" : [1],
                "mData" : "organization.name",
                "sDefaultContent" : params.notApplicable
            },
            {
                "bSortable" : false,
                "aTargets" : [2],
                "mData" : "id",
                "mRender" : function(data, type, full) {
                    var editGroupUrl = ks.contextPath + '/manager/editGroup/';
                    var editText = params.editGroupText;
                    var $elem = $('<a>').attr('href', editGroupUrl + data).text(editText);
                    return $('<div>').append($elem).html();
                },
                "sDefaultContent" : params.notApplicable
            },
            {
                "bSortable" : false,
                "aTargets" : [3],
                "mData" : "id",
                "mRender" : function(data, type, full) {
                    var deleteText = params.deleteGroupText;
                    var $elem = $('<a>').attr('href', '#').text(deleteText).addClass('deleteGroup');
                    return $('<div>').append($elem).html();
                },
                "sDefaultContent" : params.notApplicable
            }
        ],
        "fnCreatedRow": function( nRow, aData, iDataIndex ) {
            $(nRow).data('group-id', aData.id);
        },
        "fnInitComplete": function() {
            this.fnAdjustColumnSizing(true);
        },
        "fnDrawCallback": function () {
            $('.deleteGroup').on('click', deleteGroupEvent);
        }
    });

    var deleteGroupEvent = function(e) {
        e.preventDefault();

        var title = params.deleteGroupModalTitle;
        var data = { 'title' : title, 'groupId' : $(this).closest('tr').data('group-id')}
        showModal("#confirmationModal", data, fnOnDeleteGroupConfirmationModalShow, fnOnDeleteGroupConfirmationModalHide);
    }

    var fnOnDeleteGroupConfirmationModalShow = function(data) {
        var deleteGroupUrl = ks.contextPath + '/manager/deleteGroup';
        $('.modal-header h3', '#confirmationModal').text(data.title);
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $.post(deleteGroupUrl, {"id":data.groupId}, function(result) {
                if (result.result) {
                    $('#confirmationModal .modal-body .alert-error').hide();
                    $('#confirmationModal .modal-body .alert-success').show();
                    $groupTable.fnReloadAjax(getGroupsUrl);
                    hideModal(500);
                } else {
                    $('#confirmationModal .modal-body .alert-error').show();
                    $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                    $('#confirmationModal .close').removeAttr('disabled');
                }
            });
        });
    }

    var fnOnDeleteGroupConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    function showModal(id, data, fnOnShow, fnOnHide) {
        if (id.substr(0, 1) !== '#') {
            id = '#' + id;
        }

        $(id).off('show');
        $(id).on('show', fnOnShow(data));

        $(id).off('hidden');
        $(id).on('hidden', fnOnHide());

        $(id).modal();
    }

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }
});

$(document).on('knappsack.manage_group-page', function(event, params) {
    var tmpOptions = {};
    var tmpSelectedRows = [];
    var groupId = params.groupId;
    var getApplicationsURL = ks.contextPath + '/manager/getOwnedApplicationsForGroup?grp=' + groupId;

    var $applicationsTable;
    $('a[data-toggle="tab"][href="#applications"]').on('show', function(e) {
        if (!$applicationsTable) {
            $applicationsTable = $('#applicationsTable').dataTable( {
                "sDom": "<'table-inline'<<'span6'l><'pull-right'f>r>t<<'span6'i><'pull-right'p>>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
                },
                "bSort": true,
                "sAjaxSource": getApplicationsURL,
                "sAjaxDataProp":"",
                "bProcessing": true,
                "bAutoWidth": true,
                "aoColumnDefs" : [
                    {
                        "aTargets" : [0],
                        "mData" : "name",
                        "mRender" : function(data, type, full) {
                            var $anchor = $('<a>').attr('href', ks.contextPath + '/detail/' + full.id).attr('title', data).addClass('dashed').text(data);
                            return $('<div>').append($anchor).html();
                        },
                        "sDefaultContent" : params.notApplicable
                    },
                    {
                        "aTargets" : [1],
                        "mData" : "applicationType",
                        "sDefaultContent" : params.notApplicable
                    },
                    {
                        "bSortable" : false,
                        "aTargets" : [2],
                        "mData" : "id",
                        "mRender" : function(data, type, full) {
                            var editBtn = $('<a>').addClass('btn edit-version').attr('href', ks.contextPath + '/manager/editApplication/' + data).attr('title', params.editText).append($('<i>').addClass('icon-edit'));
                            var deleteBtn = $('<a>').addClass('btn btn-danger delete-application').attr('href', '#').attr('title', params.deleteText).append($('<i>').addClass('icon-trash'));

                            return $('<div>').append(editBtn).append('&nbsp;').append(deleteBtn).html();
                        },
                        "sDefaultContent" : "N/A"
                    }
                ],
                "fnCreatedRow": function( nRow, aData, iDataIndex ) {
                    $(nRow).data('application', aData);
                },
                "fnInitComplete": function() {
                    this.fnAdjustColumnSizing(true);
                },
                "fnDrawCallback": function () {
                    $('.delete-application').on('click', deleteApplicationEvent);
                }
            });
        }
    });
    $(this).tabState('#group');

    var deleteApplicationEvent = function(e) {
        e.preventDefault();
        var applicationData = $(this).closest('tr').data('application');
        var title = params.deleteApplicationModalTitle;
        if (applicationData) {
            title += ' <b>' + applicationData.name + '</b>';
        }
        var body = $('<p>').text(params.deleteApplicationModalBody);
        var data = { 'title' : title, 'body' : body, 'id' : applicationData.id };
        showModal("#confirmationModal", data, fnOnDeleteApplicationConfirmationModalShow, fnOnDeleteApplicationConfirmationModalHide);
    };

    var fnOnDeleteApplicationConfirmationModalShow = function(data) {
        $('.modal-header h3', '#confirmationModal').html(data.title);
        $('#confirmationModal .custom').empty();
        if (data.body) {
            $('#confirmationModal > .modal-body > .body > .default').hide();
            $('#confirmationModal > .modal-body > .body > .custom').append(data.body).show();
        } else {
            $('#confirmationModal > .modal-body > .body > .custom').hide();
            $('#confirmationModal > .modal-body > .body > .default').show();
        }
        $('#confirmationModal > .modal-body > .body').show();
        $('#confirmationModalSubmit').off('click');
        $('#confirmationModalSubmit').on('click', function(e) {
            $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
            $.post(ks.contextPath + '/manager/deleteApplication', {'id' : data.id}, function(result) {
                $('#confirmationModal > .modal-body > .body').hide();
                if (result.result) {
                    $('#confirmationModal .modal-body .alert-error').hide();
                    $('#confirmationModal .modal-body .alert-success').show();
                    $applicationsTable.fnReloadAjax(getApplicationsURL);
                    hideModal(1000);
                } else {
                    $('#confirmationModal .modal-body .alert-success').hide();
                    $('#confirmationModal > .modal-body > .alert-error > .custom').hide();
                    $('#confirmationModal > .modal-body > .alert-error > .default').show();
                    $('#confirmationModal > .modal-body > .alert-error').show();
                    $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                    $('#confirmationModal .close').removeAttr('disabled');
                }
            });
        });
    };

    var fnOnDeleteApplicationConfirmationModalHide = function() {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    };

    var $membersTable = $('#membersTable').dataTable( {
        "sDom": "<'table-inline'<<'span6'l><'pull-right'f>r>t<<'span6'i><'pull-right'p>>>",
        "sPaginationType": "bootstrap",
        "oLanguage": {
            "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
        },
        "aoColumns": [
            { "bSortable": false },
            null,
            null,
            null
        ]
    });
    $('#membersTableRemoveBtn').data('table', $membersTable);
    $('#membersTable').data('table', $membersTable);

    var $pendingRequestsTable = $('#pendingRequestsTable').dataTable( {
        "sDom": "<'table-inline'<<'span6'l><'pull-right'f>r>t<<'span6'i><'pull-right'p>>>",
        "sPaginationType": "bootstrap",
        "oLanguage": {
            "sLengthMenu": "_MENU_ " + ks.recordsPerPageText
        },
        "aoColumns": [
            null,
            { "sSortDataType": "dom-select" },
            { "bSortable": false },
            { "bSortable": false }
        ]
    });

    function showConfirmationModal(title) {
        $('#confirmationModal .modal-header h3').text(title);
        $('#confirmationModal').modal();
    }

    $('#confirmationModal').on('show', function () {
        $('#confirmationModal .modal-body .alert').hide();
        $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
        $('#confirmationModal .close').removeAttr('disabled');
    });

    $('#confirmationModal').on('hide', function () {
        $('#confirmationModalSubmit').unbind('click');
    });

    function ajaxMemberRequestSubmit(dataTable, row) {
        $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
        $('#confirmationModal .close').attr('disabled', 'disabled');
        var url = ks.contextPath + '/manager/userRequest';
        $.post(url, tmpOptions, function (data) {
            if (data.result) {
                $('#confirmationModal .modal-body .alert-error').hide();
                $('#confirmationModal .modal-body .alert-success').show();
                if (tmpOptions.status) {
                    var newRow = $('<tr>').attr('data-user-id', $(row).attr('data-user-id'));
                    var checkBoxTD = $('<td>').append($('<input>').addClass('check').attr('type', 'checkbox'));
                    var nameTD = $('<td>').text($(row).attr('data-user-name'));
                    var emailTD = $('<td>').text($(row).attr('data-user-email'));
                    var roleTD = $('<td>').text($(row).find('.select-td').find('select option:selected').text());

                    newRow.append(checkBoxTD).append(nameTD).append(emailTD).append(roleTD);

                    $membersTable.fnAddTr($(newRow).get(0), true);
                }

                dataTable.fnDeleteRow(row);
                tmpOptions = {};
                hideModal(500);
                checkAndHidePendingRequests();
            } else {
                $('#confirmationModal .modal-body .alert-error').show();
                $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                $('#confirmationModal .close').removeAttr('disabled');
            }
        });
    }

    function ajaxRemoveMemberSubmit(dataTable) {
        $('#confirmationModal .modal-footer .btn').attr('disabled', 'disabled');
        $('#confirmationModal .close').attr('disabled', 'disabled');
        var url = ks.contextPath + '/manager/removeUsers';
        $.ajax({
            url:url,
            data:tmpOptions,
            type:"POST",
            success:function (data) {
                if (data.result) {
                    $('#confirmationModal .modal-body .alert-error').hide();
                    $('#confirmationModal .modal-body .alert-success').show();
                    $(tmpSelectedRows).each(function () {
                        dataTable.fnDeleteRow(this);
                    });
                    tmpOptions = {};
                    tmpSelectedRows = [];
                    hideModal(500);
                } else {
                    $('#confirmationModal .modal-body .alert-error').show();
                    $('#confirmationModal .modal-footer .btn').removeAttr('disabled');
                    $('#confirmationModal .close').removeAttr('disabled');
                }
            }
        });
    }

    function acceptPendingRequest(row) {
        var requestId = $(row).attr('data-request-id');
        var userRole = $(row).children('td.select-td').children('select').val();
        tmpOptions = { requestId:requestId, status:true, userRole:userRole };
        var title = params.acceptPendingRequestModalTitle;
        showConfirmationModal(title);
        $('#confirmationModalSubmit').click(function () {
            ajaxMemberRequestSubmit($pendingRequestsTable, row);
        });
    }

    $('#pendingRequestsTable').on('click', '.declineRequest', function() {
        var row = $(this).closest('tr').get(0);
        var requestId = $(row).attr('data-request-id');
        tmpOptions = { requestId:requestId, status:false };
        var title = params.declinePendingRequestModalTitle;
        showConfirmationModal(title);
        $('#confirmationModalSubmit').click(function () {
            ajaxMemberRequestSubmit($pendingRequestsTable, row);
        });
    });

    $('#membersTableRemoveBtn').on('click', function (event) {
        event.preventDefault();
        var memberIdsToRemove = [];

        var $dataTable = $(this).data('table');

        var data = $dataTable.$('tr.row_selected');

        $(data).each(function () {
            memberIdsToRemove.push($(this).attr('data-user-id'));
            tmpSelectedRows.push(this);
        });

        tmpOptions = { groupId:groupId, userIds:memberIdsToRemove };
        var title = params.removeMembersModalTitle;
        showConfirmationModal(title);
        $('#confirmationModalSubmit').click(function () {
            ajaxRemoveMemberSubmit($dataTable);
        });
    });

    $('#membersTable').on('click', '.check-all', function() {
        var checked = $(this).is(':checked');

        var $dataTable = $(this).closest('table').data('table');

        $dataTable.$('td > :checkbox').each(function () {
            if (checked) {
                $(this).attr('checked', 'checked');
                $(this).closest('tr').addClass('row_selected');
            } else {
                $(this).removeAttr('checked');
                $(this).closest('tr').removeClass('row_selected');
            }
        });

        checkButtonState($dataTable, $('#membersTableRemoveBtn'));
    });

    $('#membersTable').on('click', '.check', function() {
        var checked = $(this).attr('checked')

        if (checked) {
            $(this).closest('tr').addClass('row_selected');
        } else {
            $(this).closest('tr').removeClass('row_selected');
        }

        checkButtonState($membersTable, $('#membersTableRemoveBtn'));
    });

    var pendingRequestsExist = params.pendingRequestsExist;
    if (pendingRequestsExist) {
        $($pendingRequestsTable.fnGetNodes()).each(function() {
            $(this).on('submit', 'form', function() {
                var row = $(this).closest('tr').get(0);
                acceptPendingRequest(row);
                return false;
            });
        });
    }

    function checkAndHidePendingRequests() {
        if ($pendingRequestsTable.fnGetData().length == 0) {
            $('#pendingRequestsSection').hide();
            $('#membersTabAlertIcon').hide();
        }
    }

    function checkButtonState(dataTable, button) {
        if (!dataTable || !button) {
            return;
        }

        var checkedLength = dataTable.$('td :checkbox:checked').length;

        if (checkedLength > 0) {
            $(button).removeAttr('disabled');
        } else {
            $(button).attr('disabled', 'disabled');
        }
    }

    function showModal(id, data, fnOnShow, fnOnHide) {
        if (id.substr(0, 1) !== '#') {
            id = '#' + id;
        }

        $(id).off('show');
        $(id).on('show', fnOnShow(data));

        $(id).off('hidden');
        $(id).on('hidden', fnOnHide());

        $(id).modal();
    }

    function hideModal(delay) {
        setTimeout(function () {
                $('.modal').modal('hide');
            }, delay
        );
    }
});
