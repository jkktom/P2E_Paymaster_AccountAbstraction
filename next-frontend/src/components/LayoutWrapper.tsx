'use client'

import Header from "@/components/Header";

interface LayoutWrapperProps {
  children: React.ReactNode;
}

export default function LayoutWrapper({ children }: LayoutWrapperProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="w-full">
        {children}
      </main>
      <footer className="bg-white border-t">
        <div className="max-w-7xl mx-auto px-4 py-6 text-center">
          <p className="text-sm">
            Technical Interview Submission - Blockchain Product Exchange Service
          </p>
          <p className="text-xs mt-1">
            Spring Boot + zkSync + Account Abstraction + Next.js
          </p>
        </div>
      </footer>
    </div>
  );
}
