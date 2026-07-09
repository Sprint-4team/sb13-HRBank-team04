import { Button } from "@/components/common/buttons/Button";
import { Input } from "@/components/common/input/Input";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { useDepartmentListStore } from "@/store/departmentStore";
import { Plus, SearchMd } from "@untitledui/icons";
import { useEffect, useState } from "react";

interface DepartmentFilterSectionProps {
  onAdd: () => void;
}

export function DepartmentFilterSection({ onAdd }: DepartmentFilterSectionProps) {
  const { totalElements, filters, setFilters } = useDepartmentListStore();

  const [keyword, setKeyword] = useState(filters.nameOrDescription ?? ""); // 검색 인풋

  // 디바운스
  const debouncedKeyword = useDebouncedValue(keyword);

  useEffect(() => {
    setFilters({ nameOrDescription: debouncedKeyword });
  }, [setFilters, debouncedKeyword]);

  return (
    <>
      {/* 총 부서 팀 수 */}
      <span className="block font-normal text-sm text-gray-600">총 {totalElements}팀</span>
      {/* 검색 및 부서 추가하기 버튼 */}
      <div className="flex items-center justify-between">
        <Input
          icon={SearchMd}
          iconClassName="text-black"
          placeholder="부서명 또는 설명을 입력해주세요"
          value={keyword}
          onChange={(value) => setKeyword(value)}
          className="max-w-80"
        />
        <Button iconLeading={Plus} className="text-white" onClick={onAdd}>
          부서 추가하기
        </Button>
      </div>
    </>
  );
}
