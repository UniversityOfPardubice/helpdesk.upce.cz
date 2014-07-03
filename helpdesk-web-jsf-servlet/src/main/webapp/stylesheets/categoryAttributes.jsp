<%@include file="_include.jsp"%>
<e:page stringsVar="msgs" menuItem="departments" locale="#{sessionController.locale}"
	authorized="#{departmentsController.currentUserCanManageDepartmentCategories}">
	<%@include file="_navigation.jsp"%>

	<e:form 
		freezeScreenOnSubmit="#{sessionController.freezeScreenOnSubmit}" 
		showSubmitPopupText="#{sessionController.showSubmitPopupText}" 
		showSubmitPopupImage="#{sessionController.showSubmitPopupImage}" 
		id="categoryAttributesForm">
		<e:panelGrid columns="2" columnClasses="colLeft,colRight" width="100%">
			<e:section value="#{msgs['CATEGORY_ATTRIBUTES.TITLE']}">
				<f:param value="#{departmentsController.categoryToUpdate.department.label}" />
				<f:param value="#{departmentsController.categoryToUpdate.label}" />
			</e:section>
			<h:panelGroup>
				<h:panelGroup style="cursor: pointer" onclick="simulateLinkClick('categoryAttributesForm:cancelButton');" >
					<e:bold value="#{msgs['_.BUTTON.BACK']} " />
					<t:graphicImage value="/media/images/back.png"
						alt="#{msgs['_.BUTTON.BACK']}" 
						title="#{msgs['_.BUTTON.BACK']}" />
				</h:panelGroup>
				<e:commandButton id="cancelButton" action="back" style="display: none"
					value="#{msgs['_.BUTTON.BACK']}" immediate="true" />
			</h:panelGroup>
		</e:panelGrid>

		<e:messages />

		<h:panelGroup rendered="#{departmentsController.categoryToUpdate.inheritAttributes && (departmentsController.categoryToUpdate.parent != null)}" >
			<e:paragraph 
				value="#{msgs['CATEGORY_ATTRIBUTES.TEXT.INHERIT']}" />
			<h:panelGroup style="cursor: pointer" onclick="simulateLinkClick('categoryAttributesForm:doNotInheritButton');" >
				<e:bold value="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.DO_NOT_INHERIT']} " />
				<t:graphicImage value="/media/images/cancel.png"
					alt="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.DO_NOT_INHERIT']}"
					title="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.DO_NOT_INHERIT']}" />
			</h:panelGroup>
			<e:commandButton id="doNotInheritButton" style="display: none"
				action="#{departmentsController.toggleInheritCategoryAttributes}"
				value="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.DO_NOT_INHERIT']}" />
			<e:paragraph 
				value="#{msgs['CATEGORY_ATTRIBUTES.TEXT.NO_INHERITED_ATTRIBUTE']}"
				rendered="#{empty departmentsController.inheritedCategoryAttributes}" />
			<e:paragraph 
				value="#{msgs['CATEGORY_ATTRIBUTES.TEXT.INHERITED_ATTRIBUTES']}"
				rendered="#{not empty departmentsController.inheritedCategoryAttributes}" />
			<t:dataList
				value="#{departmentsController.inheritedCategoryAttributes}"
				var="categoryAttribute" >
				<e:li value="#{categoryAttribute.label}" />
			</t:dataList>
		</h:panelGroup>
		<h:panelGroup rendered="#{not departmentsController.categoryToUpdate.inheritAttributes}" >
			<h:panelGroup rendered="#{departmentsController.categoryToUpdate.parent != null}" >
				<e:paragraph value="#{msgs['CATEGORY_ATTRIBUTES.TEXT.NO_INHERIT_CATEGORY']}" />
				<h:panelGroup style="cursor: pointer" 
					onclick="simulateLinkClick('categoryAttributesForm:inheritCategoryButton');" >
					<e:bold value="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.INHERIT_CATEGORY']} " />
					<t:graphicImage value="/media/images/add.png" />
				</h:panelGroup>
				<e:commandButton id="inheritCategoryButton" style="display: none"
					action="#{departmentsController.toggleInheritCategoryAttributes}"
					value="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.INHERIT_CATEGORY']}" />
			</h:panelGroup>
			<e:paragraph 
				value="#{msgs['CATEGORY_ATTRIBUTES.TEXT.NO_ATTRIBUTE']}"
				rendered="#{empty departmentsController.categoryAttributes}"
				/>
			<h:panelGroup rendered="#{not empty departmentsController.categoryAttributes}" >
				<e:paragraph value="#{msgs['CATEGORY_ATTRIBUTES.TEXT.ATTRIBUTES']}" />
                <e:dataTable
					id="attributeData" rowIndexVar="variable"
					value="#{departmentsController.categoryAttributes}"
					var="attribute" border="0" cellspacing="0"
					cellpadding="0">
                    <f:facet name="header">
                        <t:htmlTag value="hr" />
                    </f:facet>
					<t:column>
						<e:bold value="#{attribute.name}" />
					</t:column>
					<t:column>
						<e:bold value="#{attribute.label}" />
					</t:column>
                    <t:column style="padding: 0px 10px">
                        <e:text value="#{categoryAttributeTypeFormatter[attribute.type]}" />
                    </t:column>
					<t:column>
						<h:panelGroup style="cursor: pointer"
							onclick="simulateLinkClick('categoryAttributesForm:attributeData:#{variable}:deleteButton');" >
							<t:graphicImage value="/media/images/delete.png"
								alt="-" title="-" />
						</h:panelGroup>
						<e:commandButton id="deleteButton" value="-" style="display: none"
							action="#{departmentsController.deleteCategoryAttribute}" >
							<t:updateActionListener value="#{attribute}"
								property="#{departmentsController.categoryAttributeToDelete}" />
						</e:commandButton>
					</t:column>
					<t:column>
						<h:panelGroup style="cursor: pointer"
							onclick="simulateLinkClick('categoryAttributesForm:attributeData:#{variable}:editButton');" >
							<t:graphicImage value="/media/images/edit.png"
								alt="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.EDIT']}"
                                title="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.EDIT']}" />
						</h:panelGroup>
						<e:commandButton id="editButton" value="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.EDIT']}"
                            style="display: none"
							action="#{departmentsController.updateCategoryAttribute}" >
							<t:updateActionListener value="#{attribute}"
								property="#{departmentsController.categoryAttributeToUpdate}" />
						</e:commandButton>
					</t:column>
					<t:column>
						<h:panelGroup
							rendered="#{variable != departmentsController.categoryAttributesNumber - 1}" >
							<h:panelGroup style="cursor: pointer"
								onclick="simulateLinkClick('categoryAttributesForm:attributeData:#{variable}:moveLastButton');" >
								<t:graphicImage value="/media/images/arrow_last.png"
									alt="vv" title="vv" />
							</h:panelGroup>
							<e:commandButton id="moveLastButton" value="vv" style="display: none"
								action="#{departmentsController.moveCategoryAttributeLast}"
								>
								<t:updateActionListener value="#{attribute}"
									property="#{departmentsController.categoryAttributeToMove}" />
							</e:commandButton>
						</h:panelGroup>
					</t:column>
					<t:column>
						<h:panelGroup
							rendered="#{variable != departmentsController.categoryAttributesNumber - 1}" >
							<h:panelGroup style="cursor: pointer"
								onclick="simulateLinkClick('categoryAttributesForm:attributeData:#{variable}:moveDownButton');" >
								<t:graphicImage value="/media/images/arrow_down.png"
									alt="v" title="v" />
							</h:panelGroup>
							<e:commandButton id="moveDownButton" value="v" style="display: none"
								action="#{departmentsController.moveCategoryAttributeDown}"
								>
								<t:updateActionListener value="#{attribute}"
									property="#{departmentsController.categoryAttributeToMove}" />
							</e:commandButton>
						</h:panelGroup>
					</t:column>
					<t:column>
						<h:panelGroup
							rendered="#{variable != 0}" >
							<h:panelGroup style="cursor: pointer"
								onclick="simulateLinkClick('categoryAttributesForm:attributeData:#{variable}:moveUpButton');" >
								<t:graphicImage value="/media/images/arrow_up.png"
									alt="^" title="^" />
							</h:panelGroup>
							<e:commandButton id="moveUpButton" value="^" style="display: none"
								action="#{departmentsController.moveCategoryAttributeUp}"
								>
								<t:updateActionListener value="#{attribute}"
									property="#{departmentsController.categoryAttributeToMove}" />
							</e:commandButton>
						</h:panelGroup>
					</t:column>
					<t:column>
						<h:panelGroup
							rendered="#{variable != 0}" >
							<h:panelGroup style="cursor: pointer"
								onclick="simulateLinkClick('categoryAttributesForm:attributeData:#{variable}:moveFirstButton');" >
								<t:graphicImage value="/media/images/arrow_first.png"
									alt="^^" title="^^" />
							</h:panelGroup>
							<e:commandButton id="moveFirstButton" value="^^" style="display: none"
								action="#{departmentsController.moveCategoryAttributeFirst}"
								>
								<t:updateActionListener value="#{attribute}"
									property="#{departmentsController.categoryAttributeToMove}" />
							</e:commandButton>
						</h:panelGroup>
					</t:column>
					<f:facet name="footer">
						<t:htmlTag value="hr" />
					</f:facet>
                </e:dataTable>
			</h:panelGroup>
            <h:panelGroup>
                <h:panelGroup style="cursor: pointer" onclick="simulateLinkClick('categoryAttributesForm:addButton');" >
                    <e:bold value="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.ADD']}" />
                    <t:graphicImage value="/media/images/add.png"
                        alt="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.ADD']}"
                        title="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.ADD']}" />
                </h:panelGroup>
                <e:commandButton id="addButton" action="add"
                    value="#{msgs['CATEGORY_ATTRIBUTES.BUTTON.ADD']}" style="display: none" />
            </h:panelGroup>
		</h:panelGroup>
	</e:form>
	<t:aliasBean alias="#{controller}" value="#{null}" >
		<%@include file="_signature.jsp"%>
	</t:aliasBean>
</e:page>
