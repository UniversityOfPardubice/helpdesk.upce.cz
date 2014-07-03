<%@include file="_include.jsp"%>
<e:panelGrid columns="1" columnClasses="colLeftNowrap" rendered="#{not empty archivedTicketController.archivedTicketAttributes}">
    <e:subSection value="#{msgs['TICKET_VIEW.ATTRIBUTES.HEADER']}" />
    <h:dataTable value="#{archivedTicketController.archivedTicketAttributes}" var="attribute">
        <h:column>
            <e:bold value="#{attribute.label}"/>
        </h:column>
        <h:column>
            <e:text value="#{attribute.value}"/>
        </h:column>
    </h:dataTable>
</e:panelGrid>