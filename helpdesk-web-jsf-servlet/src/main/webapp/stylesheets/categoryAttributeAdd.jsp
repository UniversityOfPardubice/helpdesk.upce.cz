<%@include file="_include.jsp"%>
<e:page stringsVar="msgs" menuItem="departments" locale="#{sessionController.locale}"
	authorized="#{departmentsController.currentUserCanManageDepartmentCategories}">
	<script type="text/javascript">
function typeChanged(select) {
    select = document.getElementById('categoryAttributesAddForm:type');
    switch (select.value) {
        case 'TEXT':
            hideElement('categoryAttributesAddForm:panelValues');
            hideElement('categoryAttributesAddForm:panelDbConnection');
            break;
        case 'SELECT':
            showElement('categoryAttributesAddForm:panelValues');
            hideElement('categoryAttributesAddForm:panelDbConnection');
            break;
        case 'DB':
            hideElement('categoryAttributesAddForm:panelValues');
            showElement('categoryAttributesAddForm:panelDbConnection');
            break;
    }
}
</script>

    <%@include file="_navigation.jsp"%>

	<e:form 
		freezeScreenOnSubmit="#{sessionController.freezeScreenOnSubmit}" 
		showSubmitPopupText="#{sessionController.showSubmitPopupText}" 
		showSubmitPopupImage="#{sessionController.showSubmitPopupImage}" 
		id="categoryAttributesAddForm">
		<e:panelGrid columns="2" columnClasses="colLeft,colRight" width="100%">
			<e:section value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TITLE']}">
				<f:param value="#{departmentsController.categoryToUpdate.department.label}" />
				<f:param value="#{departmentsController.categoryToUpdate.label}" />
			</e:section>
			<h:panelGroup>
				<h:panelGroup style="cursor: pointer" onclick="simulateLinkClick('categoryAttributesAddForm:cancelButton');" >
					<e:bold value="#{msgs['_.BUTTON.CANCEL']} " />
					<t:graphicImage value="/media/images/back.png"
						alt="#{msgs['_.BUTTON.CANCEL']}"
						title="#{msgs['_.BUTTON.CANCEL']}" />
				</h:panelGroup>
				<e:commandButton id="cancelButton" action="cancel" style="display: none"
					value="#{msgs['_.BUTTON.CANCEL']}" immediate="true" />
			</h:panelGroup>
		</e:panelGrid>

		<e:messages />

        <h:panelGroup >
            <e:panelGrid columns="2" columnClasses="colLeftNowrap,colLeft" >
                <e:outputLabel for="name"
                    value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.NAME']}" />
                <h:panelGroup>
                    <e:inputText id="name"
                        required="true"
                        value="#{departmentsController.categoryAttributeToAdd.name}" />
                    <e:message for="name" />
                    <e:italic value=" #{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.NAME_HELP']}" />
                </h:panelGroup>
                <e:outputLabel for="label"
                    value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.LABEL']}" />
                <h:panelGroup>
                    <e:inputText id="label"
                        required="true"
                        value="#{departmentsController.categoryAttributeToAdd.label}" />
                    <e:message for="label" />
                    <e:italic value=" #{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.LABEL_HELP']}" />
                </h:panelGroup>
                <e:outputLabel for="type"
                    value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.TYPE']}" />
                <h:panelGroup>
                    <e:selectOneMenu id="type"
                        value="#{departmentsController.categoryAttributeToAdd.type}"
                        onchange="typeChanged();">
                        <f:selectItems value="#{departmentsController.categoryAttributeTypes}" />
                    </e:selectOneMenu>
                    <e:message for="type" />
                    <e:italic value=" #{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.TYPE_HELP']}" />
                </h:panelGroup>
            </e:panelGrid>

            <h:panelGroup id="panelValues" style="display:none">
                <e:bold value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.PARAMETERS']} " />
                <e:panelGrid columns="2" columnClasses="colLeftNowrap,colLeft" >
                    <e:outputLabel for="values"
                        value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.VALUES']}" />
                    <h:panelGroup>
                        <e:inputText id="values"
                            value="#{departmentsController.categoryAttributeToAdd.values}" />
                        <e:message for="values" />
                        <e:italic value=" #{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.VALUES_HELP']}" />
                    </h:panelGroup>
                </e:panelGrid>
            </h:panelGroup>
            <h:panelGroup id="panelDbConnection" style="display:none">
                <e:bold value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.PARAMETERS']} " />
                <e:panelGrid columns="2" columnClasses="colLeftNowrap,colLeft" >
                    <e:outputLabel for="dbConnectionContext"
                        value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.DB_CONNECTION_CTX']}" />
                    <h:panelGroup>
                        <e:inputText id="dbConnectionContext"
                            value="#{departmentsController.categoryAttributeToAdd.dbConnectionContext}" />
                        <e:message for="dbConnectionContext" />
                        <e:italic value=" #{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.DB_CONNECTION_CTX_HELP']}" />
                    </h:panelGroup>
                    <e:outputLabel for="dbConnectionJndi"
                        value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.DB_CONNECTION_JNDI']}" />
                    <h:panelGroup>
                        <e:inputText id="dbConnectionJndi"
                            value="#{departmentsController.categoryAttributeToAdd.dbConnectionJndi}" />
                        <e:message for="dbConnectionJndi" />
                        <e:italic value=" #{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.DB_CONNECTION_JNDI_HELP']}" />
                    </h:panelGroup>
                    <e:outputLabel for="dbConnectionSql"
                        value="#{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.DB_CONNECTION_SQL']}" />
                    <h:panelGroup>
                        <e:inputText id="dbConnectionSql"
                            value="#{departmentsController.categoryAttributeToAdd.dbConnectionSql}" />
                        <e:message for="dbConnectionSql" />
                        <e:italic value=" #{msgs['CATEGORY_ATTRIBUTE_ADD.TEXT.DB_CONNECTION_SQL_HELP']}" />
                    </h:panelGroup>
                </e:panelGrid>
            </h:panelGroup>
            <h:panelGroup>
                <h:panelGroup style="cursor: pointer" onclick="simulateLinkClick('categoryAttributesAddForm:addButton');" >
                    <e:bold value="#{msgs['CATEGORY_ATTRIBUTE_ADD.BUTTON.ADD']} " />
                    <t:graphicImage value="/media/images/add.png"
                        alt="#{msgs['CATEGORY_ATTRIBUTE_ADD.BUTTON.ADD']}"
                        title="#{msgs['CATEGORY_ATTRIBUTE_ADD.BUTTON.ADD']}" />
                </h:panelGroup>
                <e:commandButton id="addButton" action="#{departmentsController.addCategoryAttribute}"
                    value="#{msgs['CATEGORY_ATTRIBUTE_ADD.BUTTON.ADD']}" style="display: none" />
            </h:panelGroup>
        </h:panelGroup>
	</e:form>
	<t:aliasBean alias="#{controller}" value="#{null}" >
		<%@include file="_signature.jsp"%>
	</t:aliasBean>
	<script type="text/javascript">
		typeChanged();
	</script>
</e:page>
