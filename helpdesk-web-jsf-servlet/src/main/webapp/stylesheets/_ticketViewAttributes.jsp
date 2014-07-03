<%@include file="_include.jsp"%>
<e:panelGrid columns="1" columnClasses="colLeftNowrap" rendered="#{not empty ticketController.ticketAttributes}">
    <e:subSection value="#{msgs['TICKET_VIEW.ATTRIBUTES.HEADER']}" />
    <h:dataTable value="#{ticketController.ticketAttributes}" var="attribute">
        <h:column>
            <e:bold value="#{attribute.label}"/>
        </h:column>
        <h:column>
            <e:text value="#{attribute.value}"/>
        </h:column>
    </h:dataTable>
</e:panelGrid>