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
  /** @namespace CONTEXT */
  /** @namespace JSOG */

  const MODELS = {};
  const INDEXES = {};
  /**
   * Pluggable rendering functions for activites prior to attaching the rendered components to the DOM.
   * Can be specified via an attribute data-pre-render in the html (or added by JS). The value of said
   * attribute will be used to look up a function in this map.
   * @type {{default: default}}
   */
  const PRE_RENDER = {
    'default': function (callback) {
      callback();
    }
  };
  /**
   * Pluggable rendering functions for activities that happen after the components have been attached to
   * the DOM for the page. Can be specified via an attribute data-pre-render in the html (or added by JS).
   * The value of said attribute will be used to look up a function in this map.
   *
   * @type {{default: default}}
   */
  const POST_RENDER = {
    'default': function (callback) {
      callback()
    }
  };
  const ENDPOINT = CONTEXT + "rest/api/";

  function displayErrorMessage(message, clear) {
    let __ret = prepareMessages(clear);
    let $messages = __ret.$messages;
    let $templateMsg = __ret.$templateMsg;
    let $ul = __ret.$ul;
    displayMessage($templateMsg, message, $ul, "ERROR");
    $messages.addClass('error')
  }

    /**
     * Display a message to the user. This function presumes the following:
     *
     * 1. $templateMsg is a jquery reference to all locations that should be
     *    updated with the message (most uscases this is a single element)
     * 2. $templateMsg contains a span with a css class of ns-message-text
     *    into which the text of the message should go and a span with
     *    css class of ns-message-icon
     * 3. existing css for alert-danger, alert-warning, alert-success, alert-info
     *    classes to style the list items
     * 4. existing css for icon-message-danger, icon-message-warning,
     *    icon-message-success, icon-message-info classes to style a
     *    button element to display an icon indicating the alert type
     *    i.e (X), (!) (?) (i)
     * 5. Default styling is for alert-danger & icon-message-danger
     *
     * If the notification has a link attribute, the entire message will be
     * a hyperlink to that url, and if the notification has an onClickFn
     * attribute that value will be looked up in NOTIFICATION_ON_CLICK and
     * bound to the message's click handler.
     *
     * @param $templateMsg
     * @param message a Notification object
     * @param $ul the unordered list used to display the notification (existing elements are not cleared)
     * @param type one of SUCCESS INFO WARNING or ERROR (as string)
     * @param link if the message should be a link, this should contain a URI to link to.
     * @param onClickFn The name of a function to run when the message is clicked
     * @param fadeAfter the number of milliseconds after which the notification should fade out (undefined=never)
     * @returns {*|jQuery}
     */
  function displayMessage($templateMsg, message, $ul, type, link, onClickFn, fadeAfter) {
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
    let $icon = $msg.find('span.ns-message-icon');

      function handleFade() {
        if (fadeAfter && typeof fadeAfter === 'number') {
          $msg.delay(notification.displayMs || fadeAfter).fadeOut(2000, function () {
            $msg.remove()
          });
        }
      }

      function clearAlertClasses() {
        // typically only the danger classes would be here, but just in case...
        $msg.removeClass('alert-danger');
        $msg.removeClass('alert-warning');
        $msg.removeClass('alert-success');
        $msg.removeClass('alert-info');
        $icon.removeClass('icon-message-danger');
        $icon.removeClass('icon-message-warning');
        $icon.removeClass('icon-message-success');
        $icon.removeClass('icon-message-info')
      }

      switch (type) {
      case "ERROR"  : {
        // the default state of the template is error & errors remain visible until cleared.
        break;
      }
      case "WARNING"  : {
        clearAlertClasses();
        $msg.addClass('alert-warning');
        $icon.addClass('icon-message-warning');
        handleFade();
        break;
      }
      case "INFO"  : {
        clearAlertClasses();
        $msg.addClass('alert-info');
        $icon.addClass('icon-message-info');
        // $icon.addClass('pficon-info');
        handleFade();

        break;
      }
      case "SUCCESS"  : {
        clearAlertClasses();
        $msg.addClass('alert-warning');
        $icon.addClass('icon-message-warning');
        handleFade();
        break;
      }
    }
    if (link) {
      let $link = $('<a></a>');
      $link.attr('href', link);
      $msg.find(".ns-message-text").wrap($link)
    }
    $msg.removeClass('hidden-template');
    $ul.first().after($msg); // don't displace the template
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
   * Submit any form that contains sufficient data to represent an object.
   * This is intended to be used as an event handler for onSubmit for the form.
   *
   * @param event the event triggering the submit action
   * @returns {boolean} false to prevent event propagation.
   */
  function submitForm(event) {
    event.preventDefault();
    let type = this.getAttribute("data-type");
    let method = this.getAttribute("data-method");
    let action = this.getAttribute("data-action");
    let id = this.getAttribute("data-id");
    let body;
    let form = this;
    if (method === "PUT" || method === "POST") {
      let preExec;
      //noinspection JSUnresolvedVariable
      let actionObj = undefined; // NS_ACTION.getAction(action); // (future feature)
      // noinspection PointlessBooleanExpressionJS,JSObjectNullOrUndefined
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
    let url = ENDPOINT + type + (id ? '/' + id : '');
    prepareMessages(true);
    //noinspection JSUnresolvedFunction
    $.ajax(ensureNoBackRefs(url), options).done(function (response) {
      updateMessages(response, true);
      refreshDataOnPage(form)
    }).fail(ajaxFail);
    return false;
  }

  function objectifyForm(form, original, preExec) {
    let obj = original ? original : {};
    let $form = $(form);
    let pageName = $form.closest(".page").attr('id');
    $form.find("input").each(function () {
      let name = this.name;
      if (name) { // ignore unnamed fields such as submit buttons
        let fieldName = name.substr(pageName.length + 1);
        if (this.type === 'checkbox') {
          obj[fieldName] = $(this).is(":checked");
        } else {
          let val = $(this).val();
          if ("" !== val){
            obj[fieldName] = val;
          }
        }
      }
    });
    if (preExec) {
      preExec(obj)
    }
    return obj;
  }

  /**
   * Serializes a form into a JSOG encoded object. Supports checkboxes and any input
   * that has a useful value for .val(). Assumptions:
   *
   * 1. form is contained in an element of class "page"
   * 2. Containing element with .page has an id attribute
   * 3. names of form fields are pagename_propertyName (java property format)
   *
   * This implies only one form per .page
   *
   * @param form the form element to serialize
   * @param original optionally, the original object meant to be edited.
   * @param preExec optionally, a function to execute on the object just before JSOG.stringify()
   * @returns {string} JSOG representation of the object corresponding to the field
   */
  function serializeForm(form, original, preExec) {
    let obj = objectifyForm(form, original, preExec);
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
    } else if (elem.tagName === 'FORM') {
      populateForm.apply(elem)
    } else {
      // elements other than a table or form must supply a callback that accepts a serialized RestResponse object
      // The function should be attached to the element under the name dataResponseHandler. They should also
      // declare a data-type attribute corresponding to a model object available in the REST api, and a
      // data-id attribute if the element relates to a specific object rather than all objects of a type
      let $elem = $(elem);
      let type = $elem.attr("data-type");
      let id = $elem.attr("data-id");
      if (!id) {
        return;
      }
      let url = ENDPOINT + type + ((id && /\d+/.test(id)) ? '/' + id : '');
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
  function store(object, typeName) {
    if (object.type) {
      typeName = object.type;
    }
    let type = MODELS[typeName];
    if (!type) {
      //noinspection JSUnusedAssignment  // inspections bug? MODELS[object.type] could be undefined...
      type = (MODELS[typeName] = {});
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
   * @param type the type to be used if the data doesn't have a .type attribute
   * @returns the decoded object graph with circular references restored.
   */
  function decode(data, type) {
    let decoded = JSOG.decode(data);
    // decoded should always be a RestResponse object
    for (let i = 0; i < decoded.results.length; i++) {
      if (decoded.results[i].type || type) {
        store(decoded.results[i], type);
      }
    }
    return decoded;
  }

  /**
   * Parses a json string and then decodes it using REST.decode(object)
   *
   * @param stringJson a json string that has not yet been converted to a javascript object.
   * @param type the type to be used if the data doesn't have a .type attribute
   * @returns the parsed and decoded object graph.
   */
  function parse(stringJson, type) {
    return decode(JSON.parse(stringJson), type)
  }

  function parseFilters(filterList) {
    let filtersSpec = {};
    if (filterList) {
      let filters = filterList.split("|");
      for (let i = 0; i < filters.length; i++) {
        let filterParser = /\s*(\w+)\s*(=|!=|>|<|>=|<=|\sin\s)\s*(?:(\w+)|'(.*)')\s*$/;
        let match = filterParser.exec(filters[i]);
        if (match) {
          filtersSpec[match[1]] = encodeURIComponent(match[2]) + " " + (match[3] || match[4]);
        }
      }
    }
    return filtersSpec;
  }

  /**
   * Populate a form, applying the values of the object to the values of the
   * fetched object to the form fields by naming convention: pageName_propertyName
   */
  function populateForm() {
    let $form = $(this);
    let elem = this;
    let type = $form.attr("data-type");
    let id = $form.attr("data-id");
    if (!id) {
      return;
    }
    REST.find(type, "id=" + id, function (data) {
      if (typeof elem.dataResponseHandler === 'function') {
        //noinspection JSUnresolvedFunction
        elem.dataResponseHandler(data);
      } else {
        let pageName = $form.closest(".page").attr('id');
        $form.find("input").each(function () {
          let fieldName = this.name.substr(pageName.length + 1);
          let datum = data[0][fieldName];

          if (this.name.startsWith(pageName)) {
            if (this.type === 'checkbox') {
              $(this).prop('checked', !!datum);
            } else {
              $(this).val(datum)
            }
          }
        })
      }
    })
  }

  /**
   * populate a select when each option corresponds to a row from a query.
   */
  function populateSelect() {
    // todo: this is not well tested...
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

    let url = ENDPOINT + type;
    //noinspection JSUnresolvedFunction
    $.ajax(ensureNoBackRefs(url), {
      contentType: "application/json",
      method: "GET",
      data: filtersSpec
    }).done(function (data) {
      updateMessages(data);
      if (PRE_RENDER[preRender]) {
        PRE_RENDER[preRender](function () {
          let parsed = decode(data, type).results;
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
    let postRender = this.getAttribute("data-post-render");
    postRender = (postRender) ? postRender : 'default';

    // TODO handle embedded pipes if we ever run into that
    let filterList = this.getAttribute("data-filters");
    let filtersSpec = parseFilters(filterList);

    // todo actual pagination
    filtersSpec.rows = 10000;
    filtersSpec.start = 0;
    //noinspection JSUnresolvedFunction
    let url = ENDPOINT + type;
    //noinspection JSUnresolvedFunction
    $.ajax(ensureNoBackRefs(url), {
      contentType: "application/json",
      method: "GET",
      data: filtersSpec
    }).done(function (data) {
      updateMessages(data);
      PRE_RENDER[preRender](function () {
        let parsed = decode(data, type).results;
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
            if (action) {
              action = NS_ACTION.getAction(action); // TODO finish action support (this will fail right now)
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
      POST_RENDER[postRender](function () {

      })
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
      window.location.href = new URI().path("/").toString();  // not replace to avoid killing back button history.
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

    /**
     * Make a rest request to locate objects matching a filter & sort.
     *
     * Filter format is:
     *  <property> <operator> <value>
     *      <property> must match the name of a propety on the object to be filtered (java beans naming conventions)
     *      <operator> may be one of =,<,>,<=,>=,!=
     *      <value> should be surrounded by single quotes unless it can be guaranteed to always match \w+
     *
     * Sort format is:
     *  <property> (asc|desc)
     *
     * @param type The object type
     * @param filters filter expression as noted above
     * @param callback what to do with the objects found
     * @param rows the maxinum number of objects to retrh, (default=10)
     * @param start the number of object to skip over (for paging)
     * @param sort a sort spec as noted above
     */
  function find(type, filters, callback, rows, start, sort) {
    let filtersSpec = parseFilters(filters);
    filtersSpec.rows = rows ? rows : '10';
    filtersSpec.start = start ? start : '0';
    if (sort) {
      filtersSpec.sort = sort;
    }

    //noinspection JSUnresolvedFunction
    $.ajax({
      url: ENDPOINT + type,
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
      url: ENDPOINT + type + ( id ? '/' + id : ''),
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
    mutate(type,object,callback,action,"POST")
  }

  function mutate(type, object, callback, action, method ) {
    //noinspection JSUnresolvedFunction
    let url = ENDPOINT + type + "/" +  (object.id ? object.id : '');
    $.ajax({
      url: url,
      method: method,
      headers: {"X-Site-Action": action},
      data: JSOG.stringify(object),
      contentType: "application/json"
    }).done(function () {
      if (callback && typeof callback === 'function') {
        callback();
      }
    }).fail(ajaxFail)
  }

  function create(type, object, callback, action) {
    mutate(type,object,callback,action,"PUT")
  }

  jQuery(document).ready(function () {
    let $forms = $("form[data-method]");
    $forms.on("submit", submitForm);
    REST.refreshPage($(".home-page")[0].firstChild)
  });

  const NOTIFICATION_ON_CLICK = {
    'foo': function (evt) {
      evt.preventDefault();
      // do something
      return false;
    }
  };

  // things we export for use elsewhere.
  // noinspection JSUnusedGlobalSymbols
  window.REST = {
 // external name         internal name
    decode:               decode,
    lookup:               lookup,
    list:                 list,
    parse:                parse,
    find:                 find,
    refresh:              refresh,
    submit:               submitForm,
    serialize:            serializeForm,
    objectify:            objectifyForm,
    refreshPage:          refreshDataOnPage,
    updateMessages:       updateMessages,
    ajaxFail:             ajaxFail,
    addPreRender:         addPreRender,
    addPostRender:        addPostRender,
    findIndexed:          findIndexed,
    indexTypeByProp:      indexTypeByProp,
    displayErrorMessage:  displayErrorMessage,
    displayMessage:       displayMessage,
    update:               update,
    create:               create,
    prepareMessages:      prepareMessages
  };

})();
