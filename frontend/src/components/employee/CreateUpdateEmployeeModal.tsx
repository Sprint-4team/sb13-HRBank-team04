import { useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { Form, type DateValue } from "react-aria-components";
import axios from "axios";
import { createEmployee, updateEmployee } from "@/api/employee/employeeApi";
import { DatePicker } from "@/components/common/date-picker/DatePicker";
import { Input } from "@/components/common/input/Input";
import { TextArea } from "@/components/common/input/TextArea";
import { BaseModal } from "@/components/common/modals/BaseModal";
import { EmploymentEnableStateLabels } from "@/constants/EmploymentStateLabels";
import type { EmployeeDto } from "@/model/employee";
import { useDepartmentListStore } from "@/store/departmentStore";
import { useEmployeeListStore } from "@/store/employeeStore";
import { useToastStore } from "@/store/toastStore";
import { formatDateValue, parseDateValue } from "@/utils/date";
import { Button } from "../common/buttons/Button";
import { DropdownButton } from "../common/dropdown/DropdownButton";
import AddProfileImage from "../common/images/AddProfileImage";
import { HintText } from "../common/input/HintText";
import { Label } from "../common/input/label";
import EmployeeProfile from "./EmployeeProfile";

interface CreateEmployeeModalProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  employee: EmployeeDto | null;
}

interface FormData {
  name: string;
  email: string;
  departmentId: number;
  position: string;
  hireDate: DateValue | null;
  status?: string | "재직중";
  memo: string;
}

interface FormErrors {
  name?: string;
  email?: string;
  departmentId?: string;
  position?: string;
  hireDate?: string;
  status?: string;
}

const getInitialFormData = (employee: EmployeeDto | null): FormData => {
  if (employee) {
    const dateValue = parseDateValue(employee.hireDate);

    return {
      name: employee.name || "",
      email: employee.email || "",
      departmentId: employee.departmentId || 0,
      position: employee.position || "",
      hireDate: dateValue || null,
      status: employee.status || "",
      memo: employee.memo || "",
    };
  }
  return {
    name: "",
    email: "",
    departmentId: 33,
    position: "",
    hireDate: null,
    memo: "",
  };
};
const placeholderStyle = "text-placeholder text-md font-normal";

const CreateUpdateEmployeeModal = ({
  isOpen,
  onOpenChange,
  employee,
}: CreateEmployeeModalProps) => {
  const { items: departmentItems, loadFirstPage: loadDepartments } =
    useDepartmentListStore();
  const { loadFirstPage } = useEmployeeListStore();
  const { successToast, errorToast } = useToastStore();

  const [formData, setFormData] = useState<FormData>(() =>
    getInitialFormData(employee),
  );
  const [errors, setErrors] = useState<FormErrors>({});
  // 프로필 이미지
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [profilePreview, setProfilePreview] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [dropdownClassName, setDropdownClassName] =
    useState<string>(placeholderStyle);

  // 모달이 열릴 때 부서 전체 목록 API 요청
  useEffect(() => {
    if (!isOpen) return;

    const loadAllDepartments = async () => {
      await loadDepartments();
      while (useDepartmentListStore.getState().hasNext) {
        await useDepartmentListStore.getState().loadNextPage();
      }
    };

    void loadAllDepartments();
  }, [isOpen, loadDepartments]);

  // API로 받아온 부서 목록을 DropdownButton용 객체로 변환
  const departments = useMemo(() => {
    if (!departmentItems || departmentItems.length === 0) return {};

    const records: Record<string, string> = {};
    departmentItems.forEach((dept) => {
      records[String(dept.id)] = dept.name;
    });

    return records;
  }, [departmentItems]);

  useEffect(() => {
    setFormData(getInitialFormData(employee));
  }, [employee]);

  const resetFormdata = () => {
    setErrors({});
    setFormData(getInitialFormData(employee));
    setDropdownClassName(placeholderStyle);
    setProfilePreview(null);
  };

  const renderProfileContent = (): ReactNode => {
    // 1. 새 이미지 미리보기 (파일 선택됨)
    if (profilePreview) {
      return (
        <img
          src={profilePreview}
          alt="프로필 미리보기"
          className="h-full w-full object-cover"
        />
      );
    }

    // 2. 기존 직원 이미지 (수정 모드이며 이미지가 있을 경우)
    if (employee && employee.profileImageId) {
      return <EmployeeProfile employee={employee} />;
    }

    // 3. 기본 이미지 (새 직원 등록 또는 기존 이미지 없음)
    return <AddProfileImage className="h-full w-full" />;
  };

  // 부서 변경 핸들러
  const handleChange = (
    field: keyof FormData,
    value: string | DateValue | null,
  ) => {
    if (field === "departmentId" && typeof value === "string") {
      setFormData((prev) => ({ ...prev, departmentId: Number(value) }));
      setDropdownClassName("");
    } else if (field === "hireDate") {
      setFormData((prev) => ({ ...prev, hireDate: value as DateValue | null }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [field]: value as string | null | undefined,
      }));
    }
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  // 입사일 선택 핸들러
  const handleApply = () => {
    setFormData((prev) => ({ ...prev, hireDate: prev.hireDate }));
    setErrors((prev) => ({ ...prev, hireDate: undefined }));
  };
  // 입사일 선택 취소 핸들러(기존값 유지)
  const handleCancel = () => {
    setFormData((prev) => ({
      ...prev,
      hireDate: parseDateValue(employee?.hireDate) || null,
    }));
  };
  // 프로필 이미지 프리뷰 핸들러
  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] ?? null;
    setProfileImage(file);

    if (file) {
      const previewUrl = URL.createObjectURL(file);
      setProfilePreview(previewUrl);
    } else {
      setProfilePreview(null);
    }
  };
  // 프로필 이미지 변경 핸들러
  const handleClickProfile = () => {
    fileInputRef.current?.click();
  };

  // 직원 등록, 수정 모달 닫기 핸들러
  const handleClose = (boolean: boolean) => {
    resetFormdata();
    onOpenChange(boolean);
  };

  // 등록, 수정폼 제출
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 폼 validation 확인
    const isValid = validateForm();
    if (!isValid) {
      return;
    }

    // 날짜 string으로 변환
    const formattedDate = formatDateValue(formData.hireDate);

    try {
      if (employee) {
        // 직원 수정
        await updateEmployee(
          employee.id,
          {
            name: formData.name,
            email: formData.email,
            departmentId: formData.departmentId,
            position: formData.position,
            hireDate: formattedDate,
            status: formData.status || "재직중",
            memo: formData.memo,
          },
          profileImage,
        );
      } else {
        // 직원 생성
        await createEmployee(
          {
            name: formData.name,
            email: formData.email,
            departmentId: formData.departmentId,
            position: formData.position,
            hireDate: formattedDate,
            memo: formData.memo,
          },
          profileImage,
        );
      }

      // 생성, 수정 성공 후 목록 첫 페이지 재조회
      await loadFirstPage();
      resetFormdata();
      onOpenChange(false);
      if (!employee) {
        successToast("직원이 추가되었습니다");
      } else {
        successToast("직원 정보가 수정되었습니다");
      }
    } catch (error) {
      // 400 에러 → 중복 이메일 처리
      if (axios.isAxiosError(error)) {
        const status = error.response?.status;
        const message = error.response?.data?.message as string | undefined;

        if (status === 400 && message === "IllegalArgumentException") {
          setErrors((prev) => ({
            ...prev,
            email: "이미 존재하는 이메일이예요",
          }));
          return;
        }
      }
      if (process.env.NODE_ENV === "development") {
        console.error("직원 등록/수정 실패:", error);
      }
      if (!employee) {
        errorToast("직원 추가에 실패하였습니다");
      } else {
        errorToast("직원 정보 수정에 실패하였습니다");
      }
    }
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = "이름을 입력해주세요";
    }

    if (!formData.email.trim()) {
      newErrors.email = "이메일을 입력해주세요";
    }

    if (formData.departmentId < 1) {
      newErrors.departmentId = "부서를 선택해주세요";
    }

    if (!formData.position) {
      newErrors.position = "직함을 입력해주세요";
    }

    if (!formData.hireDate) {
      newErrors.hireDate = "입사일을 선택해주세요";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  return (
    <BaseModal
      title={employee ? "정보 수정하기" : "직원 등록하기"}
      isOpen={isOpen}
      onOpenChange={() => handleClose(false)}
      className="w-[600px] max-w-none"
    >
      <Form
        validationBehavior="aria"
        className="space-y-4"
        onSubmit={handleSubmit}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          className="hidden"
          onChange={handleProfileChange}
        />
        <div className="flex flex-col gap-4">
          <div className="flex items-start justify-between gap-8">
            <button
              onClick={(e) => {
                e.preventDefault();
                handleClickProfile();
              }}
              className="flex h-[126px] w-[126px] min-w-[126px] items-center justify-center overflow-hidden rounded-full"
            >
              {renderProfileContent()}
            </button>
            <div className="flex w-full flex-col gap-4">
              <Input
                label="이름"
                placeholder="이름을 입력해주세요"
                value={formData.name}
                onChange={(value) => handleChange("name", value)}
                isRequired
                isInvalid={!!errors.name}
                hint={errors.name}
              />
              <Input
                label="이메일"
                placeholder="이메일을 입력해주세요"
                value={formData.email}
                onChange={(value) => handleChange("email", value)}
                isRequired
                isInvalid={!!errors.email}
                hint={errors.email}
              />
              <div className="flex justify-between gap-4">
                <div className="flex min-w-36 flex-col gap-1.5">
                  <Label>부서</Label>
                  <DropdownButton
                    placeholder={String(formData.departmentId)}
                    label={departments}
                    value={String(formData.departmentId)}
                    onChange={(value) => {
                      handleChange("departmentId", value);
                    }}
                    className={employee ? "" : dropdownClassName}
                  />
                </div>
                <div className="w-full">
                  <Input
                    label="직함"
                    placeholder="직함을 입력해주세요"
                    value={formData.position}
                    onChange={(value) => handleChange("position", value)}
                    isRequired
                    isInvalid={!!errors.position}
                    hint={errors.position}
                  />
                </div>
              </div>
              <div className="flex flex-col gap-1.5">
                <Label id="hire-date-label">입사일</Label>
                <DatePicker
                  value={formData.hireDate}
                  aria-labelledby="hire-date-label"
                  placeholder={
                    employee ? employee.hireDate : "날짜를 선택해주세요"
                  }
                  defaultValue={formData.hireDate ?? undefined}
                  onChange={(value) => handleChange("hireDate", value)}
                  onApply={handleApply}
                  onCancel={handleCancel}
                  className={formData.hireDate ? "" : placeholderStyle}
                />
                {errors.hireDate && (
                  <HintText isInvalid={!!errors.hireDate}>
                    {errors.hireDate}
                  </HintText>
                )}
              </div>
              {employee && (
                <div className="flex w-36 flex-col gap-1.5">
                  <Label>상태</Label>
                  <DropdownButton
                    placeholder={String(formData.status)}
                    label={EmploymentEnableStateLabels}
                    value={String(formData.status)}
                    onChange={(value) => {
                      handleChange("status", value);
                    }}
                    className={dropdownClassName}
                  />
                </div>
              )}
            </div>
          </div>
          <TextArea
            label="메모 (변경 이력에 기록됨)"
            placeholder="변경 사유나 메모를 입력해주세요"
            value={formData.memo}
            onChange={(value) => handleChange("memo", value)}
            textAreaClassName="h-[154px]"
          />
          <div className="flex gap-2">
            <Button
              color="secondary"
              className="w-full"
              onClick={() => handleClose(false)}
            >
              취소
            </Button>
            <Button type="submit" color="primary" className="w-full">
              {employee ? "수정하기" : "등록하기"}
            </Button>
          </div>
        </div>
      </Form>
    </BaseModal>
  );
};

export default CreateUpdateEmployeeModal;
