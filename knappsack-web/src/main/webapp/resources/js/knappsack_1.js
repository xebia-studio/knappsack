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
                html = html.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
                html = html.replace(/<br\s*[\/]?>/gi, ' ');
                html = html.replace(/<([A-Z][A-Z0-9]*)\b[^>]*>/gi, '');
                html = html.replace(/<\/(.*?)>/gi, '$#');
                html = html.replace(/(\$#\s*|\s*\$#)/gi, '$#');
                html = html.replace(/(\$#)+/gi, '$#');
                var div = document.createElement("div");
                div.innerHTML = html;
                var text = div.textContent || div.innerText || "";
                text = text.replace(/\$#/gi, ' ');
                $(this).text(text);
            });
        },
        addHtml : function(html) {
            return this.each(function() {
                if (html == undefined) { return; }

                html = html.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
                $(this).html(html);
            });
        },
        escapeHtml : function() {
            return this.each(function() {
                var html = $(this).html() || "";
                html = html.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
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
        lastTab = (lastTab ? lastTab : defaultId);
        if (lastTab) {
            var $tab = $('a[href='+ lastTab +']').parents('li.tab');
            if ($tab != undefined) {
                $('ul.nav-tabs').children().removeClass('active');
                $tab.addClass('active');
                $('div.tab-content').children().removeClass('active');
                $(lastTab).addClass('active');
            }
        }
    };
})(jQuery);
