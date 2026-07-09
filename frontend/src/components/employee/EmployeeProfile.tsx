import { useState } from "react";
import type { EmployeeDto } from "@/model/employee";
import { buildProfileUrl } from "@/utils/profileUrl";

interface ProfileAvatarProps {
  employee: EmployeeDto;
}

const EmployeeProfile = ({ employee }: ProfileAvatarProps) => {
  // 이미지 로드 성공 여부를 관리
  const [imgLoadError, setImgLoadError] = useState(false);
  const hasProfileImage = !!employee.profileImageId;
  // 프로필 이미지가 없거나 이미지 로드에 실패하면 기본 프로필 이미지 출력
  const showDefaultAvatar = !hasProfileImage || imgLoadError;
  return (
    <>
      {hasProfileImage && !imgLoadError && (
        <img
          src={buildProfileUrl(employee.profileImageId)}
          alt={`${employee.name} 프로필`}
          className="h-full w-full rounded-full object-cover"
          onError={() => setImgLoadError(true)}
        />
      )}
      {showDefaultAvatar && (
        <div className="bg-secondary flex h-full w-full items-center justify-center">
          {employee.name.charAt(0)}
        </div>
      )}
    </>
  );
};

export default EmployeeProfile;
