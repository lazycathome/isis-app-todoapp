/*
 *  Copyright 2013~2014 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package todoapp.dom.module.export;

import todoapp.dom.module.todoitem.ToDoItem;
import todoapp.dom.module.todoitem.ToDoItems;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import com.google.common.base.Function;
import com.google.common.io.Resources;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.isisaddons.module.docx.dom.DocxService;
import org.isisaddons.module.docx.dom.LoadTemplateException;
import org.isisaddons.module.docx.dom.MergeException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.value.Blob;

@DomainService
@DomainServiceLayout(
        named="ToDos",
        menuOrder = "30"
)
public class ToDoItemsExportService {

    //region > init

    private WordprocessingMLPackage wordprocessingMLPackage;

    @PostConstruct
    public void init() throws IOException, LoadTemplateException {
        final byte[] bytes = Resources.toByteArray(Resources.getResource(this.getClass(), "ToDoItemsExport.docx"));
        wordprocessingMLPackage = docxService.loadPackage(new ByteArrayInputStream(bytes));
    }
    //endregion

    //region > exportToWordDoc (action)

    @Action(
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-download"
    )
    @MemberOrder(sequence = "10")
    public Blob exportToWordDoc() throws IOException, JDOMException, MergeException {

        final List<ToDoItem> notYetComplete = toDoItems.notYetComplete();
        final org.w3c.dom.Document w3cDocument = asInputW3cDocument(notYetComplete);

        final ByteArrayOutputStream docxTarget = new ByteArrayOutputStream();
        docxService.merge(w3cDocument, wordprocessingMLPackage, docxTarget, DocxService.MatchingPolicy.LAX);

        final String blobName = "todoItems-" + timestamp() + ".docx";
        final String blobMimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        final byte[] blobBytes = docxTarget.toByteArray();

        return new Blob(blobName, blobMimeType, blobBytes);
    }

    private String timestamp() {
        return clockService.nowAsLocalDateTime().toString("yyyyMMdd'_'HHmmss");
    }

    private org.w3c.dom.Document asInputW3cDocument(final List<ToDoItem> items) throws JDOMException {
        final Document jdomDoc = asInputDocument(items);

        final DOMOutputter domOutputter = new DOMOutputter();
        return domOutputter.output(jdomDoc);
    }

    private Document asInputDocument(final List<ToDoItem> items) {

        final Element html = new Element("html");
        final Document document = new Document(html);

        final Element body = new Element("body");
        html.addContent(body);

        addPara(body, "ExportedOn", "date", clockService.nowAsLocalDateTime().toString("dd-MMM-yyyy"));

        final Element table = addTable(body, "ToDoItems");
        for(final ToDoItem item: items) {
            addTableRow(table, new String[]{item.getDescription(), item.getCost().toString(), "" + item.getDueBy()});
        }
        return document;
    }

    //endregion (

    //region > helpers

    private static final Function<String, String> TRIM = new Function<String, String>() {
        @Override
        public String apply(final String input) {
            return input.trim();
        }
    };

    private static void addPara(final Element body, final String id, final String clazz, final String text) {
        final Element p = new Element("p");
        body.addContent(p);
        p.setAttribute("id", id);
        p.setAttribute("class", clazz);
        p.setText(text);
    }

    private static Element addList(final Element body, final String id) {
        final Element ul = new Element("ul");
        body.addContent(ul);
        ul.setAttribute("id", id);
        return ul;
    }

    private static Element addListItem(final Element ul, final String... paras) {
        final Element li = new Element("li");
        ul.addContent(li);
        for (final String para : paras) {
            addPara(li, para);
        }
        return ul;
    }

    private static void addPara(final Element li, final String text) {
        if(text == null) {
            return;
        }
        final Element p = new Element("p");
        li.addContent(p);
        p.setText(text);
    }

    private static Element addTable(final Element body, final String id) {
        final Element table = new Element("table");
        body.addContent(table);
        table.setAttribute("id", id);
        return table;
    }

    private static void addTableRow(final Element table, final String[] cells) {
        final Element tr = new Element("tr");
        table.addContent(tr);
        for (final String columnName : cells) {
            final Element td = new Element("td");
            tr.addContent(td);
            td.setText(columnName);
        }
    }
    //endregion

    //region > injected services

    @javax.inject.Inject
    DomainObjectContainer container;

    @javax.inject.Inject
    private DocxService docxService;

    @javax.inject.Inject
    private ToDoItems toDoItems;

    @javax.inject.Inject
    private ClockService clockService;
    //endregion

}
