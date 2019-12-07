/*
 *    Copyright (c) 2019, Needham Software LLC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

// requires:
//   jquery 2.0+

/**
 * This file contains relatively generic REST based actions. Generally these are not specific
 * to a particular domain object. More specific business actions belong in actions.js
 */

(function () {
  /** @namespace decoded.results */
  /** @namespace response.messages */
  /** @namespace notification.displayMs */

  const MODELS = {};
  const INDEXES = {};
  const PRE_RENDER = {
    'default': function (callback) {
      callback();
    }
  };
  const POST_RENDER = {
    'default': function () {
    }
  };

  function displayErrorMessage(message, clear) {
    let __ret = prepareMessages(clear);
    let $messages = __ret.$messages;
    let $templateMsg = __ret.$templateMsg;
    let $ul = __ret.$ul;
    displayMessage($templateMsg, message, $ul, "ERROR");
    $messages.addClass('error')
  }

  function displayMessage($templateMsg, message, $ul, type, link, onClickFn) {
    let notification = {};
    if (typeof message !== 'string') {
      notification = message;
      message = notification.message;
    }
    let $msg = $($templateMsg[0]).clone();
    let $msgSpan = $msg.find('span.ns-message-text');
    if (notification.link) {
      let $anchor = $('<a></a>');
      $anchor.text(message);
      $anchor.attr("href", notification.link);
      $msgSpan.empty().append($anchor);
      if (onClickFn && typeof NOTIFICATION_ON_CLICK[onClickFn] === 'function') {
        $anchor.on("click", NOTIFICATION_ON_CLICK[onClickFn])
      }
    } else {
      $msgSpan.text(message);
    }
    let $icon = $msg.find('span.pficon-error-circle-o');
    switch (type) {
      case "ERROR"  : {
        // the default state of the template is error & errors remain visible until cleared.
        break;
      }
      case "WARNING"  : {
        $msg.removeClass('alert-danger');
        $msg.addClass('alert-warning');
        $icon.removeClass('pficon-error-circle-o');
        $icon.addClass('fa');
        $icon.addClass('fa-circle');
        // $icon.addClass('pficon-warning-triangle-o');
        $msg.delay(notification.displayMs || 8000).fadeOut(2000, function () {
          $msg.remove()
        });
        break;
      }
      case "INFO"  : {
        $msg.removeClass('alert-danger');
        $msg.addClass('alert-info');
        $icon.removeClass('pficon-error-circle-o');
        $icon.addClass('fa');
        $icon.addClass('fa-circle');
        // $icon.addClass('pficon-info');
        $msg.delay(notification.displayMs || 30000).fadeOut(2000, function () {
          $msg.remove()
        });

        break;
      }
      case "SUCCESS"  : {
        $msg.removeClass('alert-danger');
        $msg.addClass('alert-success');
        $icon.removeClass('pficon-error-circle-o');
        $icon.addClass('fa');
        $icon.addClass('fa-circle');
        // $icon.addClass('pficon-ok');
        // $msg.delay(notification.displayMs || 8000).fadeOut(2000, function () {
        //   $msg.remove()
        // });
        break;
      }
      case "RECOMMENDATION"  : {
        $msg.removeClass('alert-danger');
        $msg.addClass('alert-recommend');
        $msg.addClass('sc-recommendation');
        $icon.removeClass('pficon-error-circle-o');
        $icon.addClass('fa');
        $icon.addClass('fa-circle');
        // by default do not remove recommendations.
        if (notification.displayMs) {
          $msg.delay(notification.displayMs).fadeOut(2000, function () {
            $msg.remove();
          })
        }
        break;
      }
    }
    if (link) {
      let $link = $('<a></a>');
      $link.attr('href', link);
      $msg.find(".ns-message-text").wrap($link)
    }
    $msg.removeClass('hidden-template');
    $ul.prepend($msg);
    return $msg;
  }

  function prepareMessages(clear) {
    let $messages = $('.messages');
    let $templateMsg = $messages.find('.hidden-template');
    let $ul = $messages.find('ul');
    if (clear || typeof clear === 'undefined') {
      $ul.empty();
      $ul.append($templateMsg[0]);
    }
    return {$messages: $messages, $templateMsg: $templateMsg, $ul: $ul};
  }

  function updateMessages(response, clear) {
    if (response && response.messages) {
      for (let i = 0; i < response.messages.length; i++) {
        let message = response.messages[i].message;
        let type = response.messages[i].type;
        if (type === 'ERROR') {
          displayErrorMessage(message, clear);
        }
      }
    }
  }

  function dieHard() {
    let foo;
    //noinspection JSUnusedLocalSymbols,JSUnusedAssignment,JSUnresolvedVariable
    let bar = foo.abcd; // die now. To be used to ensure remaining code will not execute.
  }

  function ensureNoBackRefs(url) {
    if (decodeURI(url).match(/.*\.\..*/)) {
      // someone tricked us into creating a back reference like /Feedback/../SiteUser/1234...
      // that's a security problem, fail immediately
      dieHard();
    }
    return url;
  }

  /**
   * Submit any form that contains sufficient data to represent an object
   *
   * @param event
   * @returns {boolean}
   */
  function submitForm(event) {
    event.preventDefault();
    let type = this.getAttribute("data-type");
    let method = this.getAttribute("data-method");
    let action = this.getAttribute("data-action");
    let id = this.getAttribute("data-id");
    let next = this.getAttribute("data-next") || "dashboard";
    let body;
    let form = this;
    if (method === "PUT" || method === "POST") {
      let preExec;
      //noinspection JSUnresolvedVariable
      let actionObj = NS_ACTION.getAction(action);
      if (actionObj && actionObj.preExecute) {
        preExec = actionObj.preExecute
      }
      body = serializeForm(form, REST.lookup(type, id), preExec);
    }
    let options = {
      contentType: "application/json",
      method: method,
      processData: false,
      data: body,
      dataType: "json"
    };
    if (action) {
      options.headers = {"X-Site-Action": action}
    }
    let url = CONTEXT + 'rest/' + type + (id ? '/' + id : '');
    prepareMessages(true);
    //noinspection JSUnresolvedFunction
    $.ajax(ensureNoBackRefs(url), options).done(function (response) {
      updateMessages(response, true);
      HashTagNavigation.show(next);
      refreshDataOnPage(form)
    }).fail(ajaxFail);
    return false;
  }

  function serializeForm(form, original, preExec) {
    let obj = original ? original : {};
    let $form = $(form);
    let pageName = $form.closest(".page").attr('id');
    $form.find("input").each(function () {
      let fieldName = this.name.substr(pageName.length + 1);
      if (this.type === 'checkbox') {
        obj[fieldName] = $(this).is(":checked");
      } else {
        obj[fieldName] = $(this).val();
      }
    });
    if (preExec) {
      preExec(obj)
    }
    return JSOG.stringify(obj);
  }

  function refreshDataOnPage(elemInPage) {
    // repopulate all tables on this ".page".
    $(elemInPage).parents(".page").each(function () {
      let $dataDrivenElements = $(this).find("*[data-type]");
      $dataDrivenElements.each(function () {
        // primarily used on forms that want to use the submit capabilities only.
        if (!($(this).attr("data-options") === 'noRefresh')) {
          populateElement(this)
        }
      })
    })
  }

  function populateElement(elem) {
    if (elem.tagName === "TABLE") {
      populateTable.apply(elem)
    } else if (elem.tagName === 'SELECT') {
      populateSelect.apply(elem)
    } else {
      // elements other than a table or form must supply a callback that accepts a serialized RestResponse object
      // The function should be attached to the element under the name dataResponseHandler. They should also
      // declare a data-type attribute corresponding to a model object available in the REST api, and a
      // data-id attribute if the element relates to a specific object rather than all objects of a type
      let $elem = $(elem);
      let type = $elem.attr("data-type");
      let id = $elem.attr("data-id");
      let url = CONTEXT + "rest/" + type + ((id && /\d+/.test(id)) ? '/' + id : '');
      //noinspection JSUnresolvedFunction
      $.get(ensureNoBackRefs(url))
          .done(function (data) {
            updateMessages(data, false); // shouldn't clear messages unless form submit
            data = REST.decode(data);
            if (data.ok && typeof elem.dataResponseHandler === 'function') {
              elem.dataResponseHandler(data);
            }
          })
          .fail(ajaxFail)
    }
  }


  // non-public method, caches a decoded object graph. Arrays ignored intentionally to avoid
  // using large amounts of memory for little gain.
  function store(object) {
    let type = MODELS[object.type];
    if (!type) {
      //noinspection JSUnusedAssignment  // inspections bug? MODELS[object.type] could be undefined...
      type = (MODELS[object.type] = {});
    }
    type[object.id] = object;  // and here the value would be used...??
  }

  /**
   * Retrieve the cached representation of a model object. Listing all objects is not
   * supported since that very unlikely to be accurate.
   *
   * @param type
   * @param id
   * @returns {*}
   */
  function lookup(type, id) {
    if (MODELS[type]) {
      return MODELS[type][id];
    } else {
      return undefined;
    }
  }

  function makeList(type) {
    let result = [];
    for (let prop in MODELS[type]) {
      if (MODELS[type].hasOwnProperty(prop)) {
        result.push(MODELS[type][prop])
      }
    }
    return result;
  }

  /**
   * Retrieve a list of all cached objects of a given type. Will invoke find for the first 999
   * if a callback is supplied.
   *
   * @param type the type to list
   * @param callback a callback to run when the data is retr
   * @returns Array or undefined if a callback is not supplied and no such objects are cached.
   */
  function list(type, callback) {
    if (callback) {
      if (MODELS[type]) {
        callback(makeList(type));
      } else {
        find(type,null,callback,999)
      }
    } else {
      if (MODELS[type]) {
        return makeList(type);
      } else {
        return undefined;
      }
    }
  }

  /**
   * Rewire the object graph from JSOG DAG representation. This handles objects, not strings.
   * For strings, use REST.parse(string).
   *
   * @param data a javascript object, usually the result of $.get() or other ajax call.
   * @returns the decoded object graph with circular references restored.
   */
  function decode(data) {
    let decoded = JSOG.decode(data);
    // decoded should always be a RestResponse object
    for (let i = 0; i < decoded.results.length; i++) {
      store(decoded.results[i]);
    }
    return decoded;
  }

  /**
   * Parses a json string and then decodes it using REST.decode(object)
   *
   * @param stringJson a json string that has not yet been converted to a javascript object.
   * @returns the parsed and decoded object graph.
   */
  function parse(stringJson) {
    return decode(JSON.parse(stringJson))
  }

  function parseFilters(filterList) {
    let filtersSpec = {};
    if (filterList) {
      let filters = filterList.split("|");
      for (let i = 0; i < filters.length; i++) {
        let filterParser = /\s*(\w+)\s*(=|!=|>|<|>=|<=)\s*(?:(\w+)|'(.*)')/;
        let match = filterParser.exec(filters[i]);
        if (match) {
          filtersSpec[match[1]] = encodeURIComponent(match[2]) + " " + (match[3] || match[4]);
        }
      }
    }
    return filtersSpec;
  }

  /**
   * populate a select when each option corresponds to a row from a query.
   */
  function populateSelect() {
    // noinspection JSUnusedGlobalSymbols
      this.refreshIt = populateSelect;
    let select = this;
    let type = this.getAttribute("data-type");
    let preRender = this.getAttribute("data-pre-render");
    preRender = (preRender) ? preRender : 'default';
    let postRender = this.getAttribute("data-post-render");
    postRender = (postRender) ? postRender : 'default';
    // TODO handle embedded pipes if we ever run into that
    let filterList = this.getAttribute("data-filters");
    let filtersSpec = parseFilters(filterList);
    filtersSpec.rows = 10000;
    filtersSpec.start = 0;
    //noinspection JSUnresolvedFunction
    let url = CONTEXT + "rest/" + type;
    //noinspection JSUnresolvedFunction
    $.ajax(ensureNoBackRefs(url), {
      contentType: "application/json",
      method: "GET",
      data: filtersSpec
    }).done(function (data) {
      updateMessages(data);
      if (PRE_RENDER[preRender]) {
        PRE_RENDER[preRender](function () {
          let parsed = decode(data).results;
          $(select).empty();
          let groups = {};
          for (let i = 0; i < parsed.length; i++) {
            let item = parsed[i];
            let renderer = select.getAttribute("data-render");
            if (renderer) {
              let option = NS_RENDER.getRenderer(renderer).render(item);
              let group = option.group;
              if (group) {
                if (!groups[group]) {
                  // noinspection RequiredAttributes
                    let $group = $('<optgroup>').attr("label", group);
                  $(select).append($group);
                  groups[group] = $group;
                }
                groups[group].append(option)
              } else {
                $(select).append(option);
              }
            }
          }
          $(select).chosen({width: "100%"});
          $(select).trigger("chosen:updated");
          POST_RENDER[postRender]();
        })
      }
    }).fail(ajaxFail);
  }

  /**
   * Populate any table that designates a type and provides a row template.
   */
  function populateTable() {
    // noinspection JSUnusedGlobalSymbols
      this.refreshIt = populateTable;
    let table = this;
    let type = this.getAttribute("data-type");
    let preRender = this.getAttribute("data-pre-render");
    preRender = (preRender) ? preRender : 'default';
    // TODO handle embedded pipes if we ever run into that
    let filterList = this.getAttribute("data-filters");
    let filtersSpec = parseFilters(filterList);

    // todo actual pagination
    filtersSpec.rows = 10000;
    filtersSpec.start = 0;
    //noinspection JSUnresolvedFunction
    let url = CONTEXT + "rest/" + type;
    //noinspection JSUnresolvedFunction
    $.ajax(ensureNoBackRefs(url), {
      contentType: "application/json",
      method: "GET",
      data: filtersSpec
    }).done(function (data) {
      updateMessages(data);
      PRE_RENDER[preRender](function () {
        let parsed = decode(data).results;
        let $template = $(table).find(".rowTemplate");
        let $tbody = $(table).find("tbody");
        $tbody.empty();
        $tbody.append($template);

        for (let i = 0; i < parsed.length; i++) {
          let rowData = parsed[i];
          let $row = $template.clone();
          $row.removeClass("rowTemplate");
          $row.find("td").each(function () {
            let td = this;
            let field = $(td).text().trim();
            let value = field ? rowData[field] : "&nbsp;";
            let renderer = td.getAttribute("data-render");
            if (renderer) {
              value = NS_RENDER.getRenderer(renderer).render(value, rowData)
            } else {
              value = field ? evalExpr(field, rowData) : value;
            }
            if (typeof value === 'string') {
              $(td).text(value);
            } else {
              $(td).empty();
              $(td).append(value);
            }
            let action = td.getAttribute("data-action");
            //noinspection JSUnresolvedVariable
            action = NS_ACTION.getAction(action);
            if (action) {
              $(td).on("click", function () {
                action.execute(td, function () {
                  refreshDataOnPage(table);
                });
              })
            }
          });
          $tbody.append($row)
        }
      });
    }).fail(ajaxFail);

  }

  /**
   * Define a function that must run before rendering begins
   *
   * @param name an arbitrary name
   * @param func the function to execute. The first argument will be a function which should be invoked once rendering
   *             is allowed to proceed.
   */
  function addPreRender(name, func) {
    PRE_RENDER[name] = func
  }

  /**
   * Define a function that must run after rendering completes
   *
   * @param name an arbitrary name
   * @param func the function to execute.
   */
  function addPostRender(name, func) {
    POST_RENDER[name] = func
  }

  //// utility stuff ////

  //noinspection JSUnusedLocalSymbols
  function ajaxFail(jqXHR, statusMsg) {
    if (jqXHR.status === 401) {
      window.location.href = new URI().path(CONTEXT).toString();  // not replace to avoid killing back button history.
    } else {
      let response = REST.parse(jqXHR.responseText);
      updateMessages(response);
    }
  }

  //function escapeRegExp(str) {
  //  // see http://stackoverflow.com/a/6969486
  //  return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
  //}
  //

  function find(type, filters, callback, rows, start, sort) {
    let filtersSpec = parseFilters(filters);
    filtersSpec.rows = rows ? rows : '10';
    filtersSpec.start = start ? start : '0';
    if (sort) {
      filtersSpec.sort = sort;
    }

    //noinspection JSUnresolvedFunction
    $.ajax({
      url: CONTEXT + 'rest/' + type,
      method: 'GET',
      dataType: 'json',
      data: filtersSpec
    }).done(function (data) {
      let objects;
      if (data && data.ok) {
        updateMessages(data, false);
        //noinspection JSUnresolvedVariable
        objects = REST.decode(data).results;
      }
      if (callback) {
        callback(objects);
      }
    }).fail(ajaxFail)
  }

  // todo: Set up a web socket based invalidation callback to allow clients to only refresh when needed.
  // todo: use http2 push instead of web sockets once Tomcat 9 is GA.
  function refresh(type, id, callback, filters, rows, start, action) {
    let filtersSpec = parseFilters(filters);
    filtersSpec.rows = rows ? rows : '1';
    filtersSpec.start = start ? start : '0';

    //noinspection JSUnresolvedFunction
    let options = {
      url: CONTEXT + 'rest/' + type + ( id ? '/' + id : ''),
      method: 'GET',
      dataType: 'json',
      data: filtersSpec
    };
    if (action) {
      options.headers = {"X-Site-Action": action}
    }
    $.ajax(options).done(function (data) {
      if (data && data.ok) {
        updateMessages(data, false);
        //noinspection JSUnresolvedVariable
        let objects = REST.decode(data).results;
        if (objects.length === 0) {
          callback();
        } else {
          for (let i = 0; i < objects.length; i++) {
            if (callback) {
              callback(objects[i]);
            }
          }
        }
      }
    }).fail(ajaxFail);
  }

  /**
   * Look up a model object that has been indexed by a property.
   *
   * @param type the type to be found
   * @param prop the property name that serves as the name of the index
   * @param value the value to look up
   * @returns an ARRAY of instances that correspond to this value in no particular order.
   */
  function findIndexed(type, prop, value) {
    if (INDEXES[type] && INDEXES[type][prop]) {
      return INDEXES[type][prop][value.id || value]
    }
  }

  function evalExpr(prop, context) {
    let steps = prop.split('.');
    for (let i = 0; context && i < steps.length; i++) {
      context = context[steps[i]];
    }
    // this handles indexing by object values, assuming the objects were
    // derived from a restful call in the first place (such as user objects).
    if (context && context.id) {
      context = context.id;
    }
    return context;
  }

  /**
   * Index a model object by a property or sub-property (dot notated) for efficient retrieval in the future.
   * property must be a unique key or behavior is undefined. (todo: fix that)
   *
   * @param type the type to index
   * @param prop the name of the property to index by
   */
  function indexTypeByProp(type, prop) {
    let models = MODELS[type];
    if (!models) {
      return;
    }
    let idx = {};
    for (let id in models) {
      if (models.hasOwnProperty(id)) {
        let propVal = models[id];
        propVal = evalExpr(prop, propVal);
        if (!idx[propVal]) {
          idx[propVal] = [];
        }
        idx[propVal].push(models[id])
      }
    }
    if (!INDEXES[type]) {
      INDEXES[type] = {};
    }
    INDEXES[type][prop] = idx;
  }

  function update(type, object, callback, action) {
    //noinspection JSUnresolvedFunction
    $.ajax({
      url: CONTEXT + "rest/" + type + "/" + object.id,
      method: "POST",
      headers: {"X-Site-Action": action},
      data: JSOG.stringify(object),
      contentType: "application/json"
    }).done(function () {
      if (callback && typeof callback === 'function') {
        callback();
      }
    }).fail(ajaxFail)
  }

  // set up the hash change event
  jQuery(document).ready(function () {
    let $forms = $("form[data-method]");
    $forms.on("submit", submitForm);
    $("table[data-type]").each(function () {
      this.loader = populateTable;
    });
    $("select[data-type]").each(function () {
      this.loader = populateSelect;
    });
  });

  const NOTIFICATION_ON_CLICK = {
    'jira_feedback': function (evt) {
      evt.preventDefault();
      $('#atlwdg-trigger')[0].dispatchEvent(new MouseEvent('click', {
        'view': window,
        'bubbles': true,
        'cancelable': false
      }));
      return false;
    }
  };

  // things we export for use elsewhere.
  // noinspection JSUnusedGlobalSymbols
    window.REST = {
        decode              : decode,
        lookup              : lookup,
        list                : list,
        parse               : parse,
        find                : find,
        refresh             : refresh,
        refreshPage         : refreshDataOnPage,
        updateMessages      : updateMessages,
        ajaxFail            : ajaxFail,
        addPreRender        : addPreRender,
        addPostRender       : addPostRender,
        findIndexed         : findIndexed,
        indexTypeByProp     : indexTypeByProp,
        displayErrorMessage : displayErrorMessage,
        displayMessage      : displayMessage,
        update              : update,
        prepareMessages     : prepareMessages
  };

})();
