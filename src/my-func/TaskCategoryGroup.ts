/* eslint-disable */
import { ITaskCategoryGroup } from "@/interfaces/ITaskCategoryGroup";
import * as Yup from "yup";
import { IForm } from "@/lib/FormRenderer";
import {
  OGDPFormField,
  OGDPFormSelectField,
} from "@oegema-transport/frontend-components";

const TaskCategoryGroupFormSchema = (formValidation: string) => {
  return Yup.object().shape({
    description: Yup.string().required(formValidation),
    responsibleUser: Yup.object().required(formValidation),
    users: Yup.array().min(1, formValidation).required(formValidation),
    taskCategory: Yup.object().required(formValidation),
  });
};

export {
  TaskCategoryGroupFormSchema,
};
