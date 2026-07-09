import { useState } from "react";
import { Form, type DateValue } from "react-aria-components";
import axios from "axios";
import {
  createDepartment,
  updateDepartment,
} from "@/api/department/departmentApi";
import { DatePicker } from "@/components/common/date-picker/DatePicker";
import { Input } from "@/components/common/input/Input";
import { Label } from "@/components/common/input/label";
import { TextArea } from "@/components/common/input/TextArea";
import { BaseModal } from "@/components/common/modals/BaseModal";
import type { DepartmentDto } from "@/model/department";
import { useDepartmentListStore } from "@/store/departmentStore";
import { useToastStore } from "@/store/toastStore";
import { formatDateValue, parseDateValue } from "@/utils/date";
import { Button } from "../common/buttons/Button";
import { HintText } from "../common/input/HintText";

interface DepartmentModalProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  department: DepartmentDto | null;
}

interface FormData {
  departmentName: string;
  tempDate: DateValue | null;
  establishedDate: DateValue | null;
  description: string;
}

interface FormErrors {
  departmentName?: string;
  establishedDate?: string;
  description?: string;
}

const getInitialFormData = (department: DepartmentDto | null): FormData => {
  if (department) {
    const dateValue = parseDateValue(department.establishedDate);
    return {
      departmentName: department.name || "",
      tempDate: dateValue || null,
      establishedDate: dateValue || null,
      description: department.description || "",
    };
  }
  return {
    departmentName: "",
    tempDate: null,
    establishedDate: null,
    description: "",
  };
};

export default function DepartmentModal({
  isOpen,
  onOpenChange,
  department,
}: DepartmentModalProps) {
  const [formData, setFormData] = useState<FormData>(() =>
    getInitialFormData(department),
  );
  const [errors, setErrors] = useState<FormErrors>({});

  const { loadFirstPage } = useDepartmentListStore();
  const { successToast } = useToastStore();

  const handleChange = (
    field: keyof FormData,
    value: string | DateValue | null,
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const handleApply = () => {
    setFormData((prev) => ({ ...prev, establishedDate: prev.tempDate }));
    setErrors((prev) => ({ ...prev, establishedDate: undefined }));
  };

  const handleCancel = () => {
    setFormData((prev) => ({ ...prev, tempDate: prev.establishedDate }));
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.departmentName.trim()) {
      newErrors.departmentName = "부서명을 입력해주세요.";
    }

    if (!formData.establishedDate) {
      newErrors.establishedDate = "설립일을 선택해주세요.";
    }

    if (!formData.description.trim()) {
      newErrors.description = "부서 설명을 입력해주세요.";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 폼 validation 확인
    const isValid = validateForm();
    if (!isValid) {
      return;
    }

    // 날짜 string으로 변환
    const formattedDate = formatDateValue(formData.establishedDate);

    try {
      if (department) {
        // 부서 수정
        await updateDepartment(department.id, {
          name: formData.departmentName,
          description: formData.description,
          establishedDate: formattedDate,
        });

        onOpenChange(false);

        successToast("부서가 수정되었습니다");
      } else {
        // 부서 생성
        await createDepartment({
          name: formData.departmentName,
          description: formData.description,
          establishedDate: formattedDate,
        });

        // 폼 데이터 초기화
        setFormData({
          departmentName: "",
          tempDate: null,
          establishedDate: null,
          description: "",
        });

        onOpenChange(false);

        successToast("부서가 추가되었습니다");
      }

      // 생성/수정 성공 후 목록 첫 페이지 재조회
      await loadFirstPage();
    } catch (error) {
      // 400 에러 → 중복 부서명 처리
      if (axios.isAxiosError(error)) {
        const status = error.response?.status;
        const message = error.response?.data?.message as string | undefined;

        if (status === 400 && message === "IllegalArgumentException") {
          setErrors((prev) => ({
            ...prev,
            departmentName: "이미 존재하는 부서 이름이에요",
          }));
          return;
        }
      }
      console.log(error);
    }
  };

  const handleClose = () => {
    onOpenChange(false);
  };

  return (
    <BaseModal
      title={department ? "부서 수정하기" : "부서 추가하기"}
      isOpen={isOpen}
      onOpenChange={onOpenChange}
    >
      <Form
        validationBehavior="aria"
        className="space-y-4"
        onSubmit={handleSubmit}
      >
        <Input
          label="부서명"
          placeholder="부서명을 입력해주세요"
          value={formData.departmentName}
          onChange={(value) => handleChange("departmentName", value)}
          isRequired
          isInvalid={!!errors.departmentName}
          hint={errors.departmentName}
        />
        <div className="space-y-2">
          <Label aria-label="날짜" isRequired>
            설립일
          </Label>
          <DatePicker
            aria-label="날짜"
            placeholder="날짜를 선택해주세요"
            value={formData.tempDate}
            onChange={(value) => handleChange("tempDate", value)}
            onApply={handleApply}
            onCancel={handleCancel}
          />
          {errors.establishedDate && (
            <HintText isInvalid={!!errors.establishedDate}>
              {errors.establishedDate}
            </HintText>
          )}
        </div>
        <TextArea
          label="설명"
          placeholder="부서에 대한 설명을 입력해주세요"
          isRequired
          value={formData.description}
          onChange={(value) => handleChange("description", value)}
          textAreaClassName="h-40"
          isInvalid={!!errors.description}
          hint={errors.description}
        />
        <div className="mt-6 flex gap-2">
          <Button color="secondary" className="w-full" onClick={handleClose}>
            취소
          </Button>
          <Button type="submit" color="primary" className="w-full">
            {department ? "수정하기" : "등록하기"}
          </Button>
        </div>
      </Form>
    </BaseModal>
  );
}
